"""Generate dictionary entries for words missing from the Bangla dictionary.

Outputs go to a run directory (stable per worker-id, resumes automatically):
    data/generation/worker_0/
        entries.json       — valid words with generated meanings
        discarded.json     — invalid words with reasons
        inflections.json   — inflected forms with their root word and DB lookup result
        meta.json          — run metadata (timestamps, model, counts per run)

Single-worker mode (default):
    python src/generation/generate_missing_entries.py

Multi-worker mode (run each in a separate terminal):
    python src/generation/generate_missing_entries.py --worker-id 0 --total-workers 4
    python src/generation/generate_missing_entries.py --worker-id 1 --total-workers 4

After all workers finish, merge with:
    python src/generation/merge_entries.py
"""

import argparse
import json
import sys
from concurrent.futures import ThreadPoolExecutor, as_completed
from datetime import datetime, timezone
from pathlib import Path

import requests

sys.path.insert(0, str(Path(__file__).parent.parent))

from generation.utils_missing import GEMINI_MODEL, generate_missing_word
from logger import logger

sys.stdout.reconfigure(encoding="utf-8")


def load_json(path: Path, default):
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except FileNotFoundError:
        return default


def save_json(path: Path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding="utf-8")


def lookup_root_word(api_base: str, root: str) -> dict | None:
    """GET /api/v1/bn/word/{spelling} — returns payload if found, else None."""
    try:
        url = f"{api_base}/api/v1/bn/word/{requests.utils.quote(root, safe='')}"
        resp = requests.get(url, timeout=5)
        return resp.json() if resp.status_code == 200 else None
    except requests.exceptions.RequestException:
        return None


def process_word(word: str, api_base: str):
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

    # Inflection detected — look up the root in the DB
    if not data.get("valid") and data.get("inflection"):
        root = data.get("root", "")
        root_payload = lookup_root_word(api_base, root) if root else None
        data["_root_found"] = root_payload is not None
        data["_root_id"] = root_payload.get("id") if root_payload else None

    return word, data


def main():
    parser = argparse.ArgumentParser(description="Generate missing dictionary entries via Gemini")
    parser.add_argument("--input", default="data/missing_words_compact.json",
                        help="Input compact missing-words file")
    parser.add_argument("--run-dir", default=None,
                        help="Output directory for this worker (default: data/generation/worker_{id})")
    parser.add_argument("--worker-id", type=int, default=0,
                        help="Index of this worker (0-based)")
    parser.add_argument("--total-workers", type=int, default=1,
                        help="Total number of parallel worker processes")
    parser.add_argument("--api-base", default="http://localhost:9000",
                        help="API base URL for root word lookup")
    parser.add_argument("--limit", type=int, default=0,
                        help="Max words to process this run (0 = all in shard)")
    parser.add_argument("--threads", type=int, default=20,
                        help="Gemini API threads per worker process")
    parser.add_argument("--dry-run", action="store_true",
                        help="Check imports and config only; skip Gemini calls and writes")
    args = parser.parse_args()

    wid = args.worker_id
    n = args.total_workers

    if wid >= n:
        logger.error(f"--worker-id {wid} must be less than --total-workers {n}")
        sys.exit(1)

    run_dir = Path(args.run_dir or f"data/generation/worker_{wid}")
    tag = f"[W{wid}/{n}]" if n > 1 else ""

    entries_path    = run_dir / "entries.json"
    discarded_path  = run_dir / "discarded.json"
    inflections_path = run_dir / "inflections.json"
    meta_path       = run_dir / "meta.json"

    # Load full input
    raw_input = load_json(Path(args.input), [])
    all_pairs = raw_input["missing_words"] if isinstance(raw_input, dict) else raw_input
    all_words = [w for w, _ in all_pairs]

    # Round-robin shard
    shard = all_words[wid::n]
    logger.info(f"{tag} Shard: {len(shard)} words (indices {wid}::{n} of {len(all_words)} total)")

    # Load existing progress
    generated: dict  = load_json(entries_path, {})
    discarded: list  = load_json(discarded_path, [])
    inflections: list = load_json(inflections_path, [])
    already_done = set(generated.keys()) | {d["word"] for d in discarded} | {i["word"] for i in inflections}

    if already_done:
        logger.info(
            f"{tag} Resuming — done: {len(already_done)} "
            f"(valid: {len(generated)}, discarded: {len(discarded)}, inflections: {len(inflections)})"
        )

    words_to_process = [w for w in shard if w not in already_done]
    if args.limit > 0:
        words_to_process = words_to_process[: args.limit]

    total = len(words_to_process)
    shard_remaining = len(shard) - len(already_done)

    logger.info(
        f"{tag} To process this run: {total} | shard remaining: {shard_remaining} | threads: {args.threads}"
    )

    if args.dry_run:
        logger.info(f"{tag} [DRY RUN] Imports OK. Would process {total} words via Gemini. No API calls or writes.")
        return

    if not words_to_process:
        logger.info(f"{tag} Nothing to do — shard complete.")
        return

    log_interval  = max(1, min(100, total // 10))
    save_interval = max(50, min(500, total // 5))

    started_at = datetime.now(timezone.utc).isoformat()
    processed = errors = 0
    valid_added = inflections_added = discarded_added = 0

    with ThreadPoolExecutor(max_workers=args.threads) as executor:
        futures = {executor.submit(process_word, w, args.api_base): w for w in words_to_process}

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
                valid_added += 1
            elif result.get("inflection"):
                root = result.get("root", "")
                root_found = result.get("_root_found", False)
                inflections.append({
                    "word": word,
                    "root": root,
                    "root_found_in_db": root_found,
                    "root_id": result.get("_root_id"),
                    "reason": result.get("reason", ""),
                })
                inflections_added += 1
            else:
                discarded.append({"word": word, "reason": result.get("reason", "")})
                discarded_added += 1

            processed += 1

            if processed % log_interval == 0 or processed == total:
                logger.info(
                    f"{tag} [{processed}/{total}] valid: {len(generated)} | "
                    f"inflections: {len(inflections)} | discarded: {len(discarded)} | errors: {errors}"
                )

            if processed % save_interval == 0:
                save_json(entries_path, generated)
                save_json(discarded_path, discarded)
                save_json(inflections_path, inflections)
                logger.info(f"{tag} Progress saved.")

    finished_at = datetime.now(timezone.utc).isoformat()

    save_json(entries_path, generated)
    save_json(discarded_path, discarded)
    save_json(inflections_path, inflections)

    # Load existing meta to append run history
    meta = load_json(meta_path, {
        "worker_id": wid,
        "total_workers": n,
        "input": args.input,
        "api_base": args.api_base,
        "model": GEMINI_MODEL,
        "shard_size": len(shard),
        "runs": [],
    })
    meta["runs"].append({
        "started_at": started_at,
        "finished_at": finished_at,
        "limit": args.limit,
        "threads": args.threads,
        "processed": processed,
        "valid_added": valid_added,
        "inflections_added": inflections_added,
        "discarded_added": discarded_added,
        "errors": errors,
    })
    meta["totals"] = {
        "valid": len(generated),
        "inflections": len(inflections),
        "discarded": len(discarded),
    }
    save_json(meta_path, meta)

    logger.info(
        f"{tag} Done — processed: {processed} | valid: {len(generated)} | "
        f"inflections: {len(inflections)} | discarded: {len(discarded)} | errors: {errors}"
    )


if __name__ == "__main__":
    main()
