"""Generate dictionary entries for words missing from the Bangla dictionary.

Single-worker mode (default):
    python src/generate_missing_entries.py

Multi-worker mode (run each in a separate terminal):
    python src/generate_missing_entries.py --worker-id 0 --total-workers 4
    python src/generate_missing_entries.py --worker-id 1 --total-workers 4
    python src/generate_missing_entries.py --worker-id 2 --total-workers 4
    python src/generate_missing_entries.py --worker-id 3 --total-workers 4

Each worker owns a fixed round-robin slice of the word list (word_index % total_workers == worker_id)
so shards are stable across restarts. Outputs go to data/workers/worker_{id}_*.json.

After all workers finish, merge with:
    python src/merge_entries.py
"""

import argparse
import json
import os
import sys
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent.parent))

from logger import logger
from generation.utils_missing import generate_missing_word

sys.stdout.reconfigure(encoding="utf-8")


def load_json_file(path, default):
    try:
        with open(path, encoding="utf-8") as f:
            return json.load(f)
    except FileNotFoundError:
        return default


def save_json_file(path, data):
    dir_name = os.path.dirname(path)
    if dir_name:
        os.makedirs(dir_name, exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)


def process_word(word: str):
    """Call Gemini and parse the response for a single word."""
    raw = generate_missing_word(word)
    try:
        data = json.loads(raw)
    except json.JSONDecodeError as e:
        logger.error(f"JSON parse error for '{word}': {e} | raw: {raw[:150]}")
        return word, {"error": f"json_parse: {e}"}

    if data.get("status") == "error":
        return word, {"error": data.get("message", "api error")}

    if "valid" not in data:
        logger.error(f"Missing 'valid' key for '{word}': {str(data)[:150]}")
        return word, {"error": "missing_valid_key"}

    return word, data


def main():
    parser = argparse.ArgumentParser(description="Generate missing dictionary entries via Gemini")
    parser.add_argument("--input", default="data/missing_words_compact.json")
    parser.add_argument("--worker-id", type=int, default=0,
                        help="Index of this worker (0-based)")
    parser.add_argument("--total-workers", type=int, default=1,
                        help="Total number of parallel worker processes")
    parser.add_argument("--out", default=None,
                        help="Output path for valid entries (default: data/workers/worker_{id}_entries.json)")
    parser.add_argument("--discarded", default=None,
                        help="Output path for discarded words (default: data/workers/worker_{id}_discarded.json)")
    parser.add_argument("--limit", type=int, default=0,
                        help="Max words to process in this run (0 = all in shard)")
    parser.add_argument("--threads", type=int, default=20,
                        help="Gemini API threads per worker process")
    parser.add_argument("--dry-run", action="store_true",
                        help="Check imports and config only; skip Gemini calls and file writes")
    args = parser.parse_args()

    wid = args.worker_id
    n = args.total_workers

    if wid >= n:
        logger.error(f"--worker-id {wid} must be less than --total-workers {n}")
        sys.exit(1)

    # Resolve output paths
    out_path = args.out or f"data/workers/worker_{wid}_entries.json"
    dis_path = args.discarded or f"data/workers/worker_{wid}_discarded.json"
    tag = f"[W{wid}/{n}]" if n > 1 else ""

    # Load full input
    raw_input = load_json_file(args.input, [])
    all_pairs = raw_input["missing_words"] if isinstance(raw_input, dict) else raw_input
    all_words = [w for w, _ in all_pairs]

    # Assign round-robin shard: words at positions worker_id, worker_id+n, worker_id+2n, ...
    shard = all_words[wid::n]
    logger.info(f"{tag} Shard: {len(shard)} words (indices {wid}::{n} of {len(all_words)} total)")

    # Load this worker's existing progress
    generated: dict = load_json_file(out_path, {})
    discarded: list = load_json_file(dis_path, [])
    already_done = set(generated.keys()) | {d["word"] for d in discarded}

    if already_done:
        logger.info(f"{tag} Resuming — done: {len(already_done)} (valid: {len(generated)}, discarded: {len(discarded)})")

    words_to_process = [w for w in shard if w not in already_done]
    if args.limit > 0:
        words_to_process = words_to_process[: args.limit]

    total = len(words_to_process)
    shard_remaining = len(shard) - len(already_done)

    logger.info(f"{tag} To process this run: {total} | shard remaining total: {shard_remaining} | threads: {args.threads}")

    if args.dry_run:
        logger.info(f"{tag} [DRY RUN] Imports OK. Would process {total} words via Gemini. No API calls or writes.")
        return

    if not words_to_process:
        logger.info(f"{tag} Nothing to do — shard complete.")
        return

    log_interval = max(1, min(100, total // 10))   # ~10 updates per run, min every 1, max every 100
    save_interval = max(50, min(500, total // 5))   # ~5 saves per run

    processed = 0
    errors = 0

    with ThreadPoolExecutor(max_workers=args.threads) as executor:
        futures = {executor.submit(process_word, w): w for w in words_to_process}

        for future in as_completed(futures):
            word = futures[future]
            try:
                _, result = future.result()
            except Exception as e:
                logger.error(f"{tag} Unhandled exception for '{word}': {e}")
                errors += 1
                processed += 1
                continue

            if "error" in result:
                errors += 1
            elif result["valid"] is True:
                generated[word] = result.get("entries", [])
            else:
                discarded.append({"word": word, "reason": result.get("reason", "")})

            processed += 1

            if processed % log_interval == 0 or processed == total:
                logger.info(
                    f"{tag} [{processed}/{total}] valid: {len(generated)} | "
                    f"discarded: {len(discarded)} | errors: {errors}"
                )

            if processed % save_interval == 0:
                save_json_file(out_path, generated)
                save_json_file(dis_path, discarded)
                logger.info(f"{tag} Saved to disk.")

    save_json_file(out_path, generated)
    save_json_file(dis_path, discarded)
    logger.info(
        f"{tag} Done — processed: {processed} | valid: {len(generated)} | "
        f"discarded: {len(discarded)} | errors: {errors}"
    )


if __name__ == "__main__":
    main()
