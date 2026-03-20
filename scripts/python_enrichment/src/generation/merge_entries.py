"""Merge per-worker output files into final generated_entries.json and discarded_words.json.

Usage:
    python src/generation/merge_entries.py               # merge all workers
    python src/generation/merge_entries.py --status      # show per-worker progress without merging
    python src/generation/merge_entries.py --workers-dir data/generation --total-workers 4
"""

import argparse
import glob
import os
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent.parent))

from logger import logger
from utils import load_json, save_json

sys.stdout.reconfigure(encoding="utf-8")


def discover_workers(workers_dir: str):
    """Return sorted list of worker IDs found in the workers directory."""
    pattern = os.path.join(workers_dir, "worker_*", "entries.json")
    paths = glob.glob(pattern)
    ids = []
    for p in paths:
        dir_name = os.path.basename(os.path.dirname(p))   # worker_3
        try:
            wid = int(dir_name.split("_")[1])
            ids.append(wid)
        except (IndexError, ValueError):
            pass
    return sorted(ids)


def load_input_shard_sizes(input_path: str, total_workers: int):
    """Return how many words each worker owns given the input file and total_workers."""
    try:
        raw = load_json(input_path, [])
        all_pairs = raw["missing_words"] if isinstance(raw, dict) else raw
        all_words = [w for w, _ in all_pairs]
    except Exception:
        return {}

    sizes = {}
    for wid in range(total_workers):
        sizes[wid] = len(all_words[wid::total_workers])
    return sizes


def main():
    parser = argparse.ArgumentParser(description="Merge per-worker Gemini output files")
    parser.add_argument("--workers-dir", default="data/generation")
    parser.add_argument("--out", default="data/generated_entries.json")
    parser.add_argument("--discarded-out", default="data/discarded_words.json")
    parser.add_argument("--input", default="data/missing_words_compact.json",
                        help="Original input file (used for shard size calculation in --status)")
    parser.add_argument("--total-workers", type=int, default=0,
                        help="Expected total workers (0 = auto-detect from files)")
    parser.add_argument("--status", action="store_true",
                        help="Show per-worker progress and exit without merging")
    parser.add_argument("--dry-run", action="store_true",
                        help="Check imports and worker files only; skip writing merged output")
    args = parser.parse_args()

    worker_ids = discover_workers(args.workers_dir)
    if not worker_ids:
        logger.error(f"No worker output files found in {args.workers_dir}")
        sys.exit(1)

    total_workers = args.total_workers or (max(worker_ids) + 1)
    if not args.total_workers:
        logger.warning(
            f"--total-workers not specified; assuming {total_workers} based on highest worker ID found. "
            f"Pass --total-workers explicitly if some workers haven't started yet."
        )
    shard_sizes = load_input_shard_sizes(args.input, total_workers)

    # ── Status report ────────────────────────────────────────────────────────
    logger.info(f"Found worker outputs: {worker_ids} (total_workers assumed: {total_workers})")
    logger.info("─" * 60)

    grand_valid = 0
    grand_discarded = 0
    grand_shard = 0

    for wid in range(total_workers):
        entries_path = os.path.join(args.workers_dir, f"worker_{wid}", "entries.json")
        dis_path = os.path.join(args.workers_dir, f"worker_{wid}", "discarded.json")

        entries = load_json(entries_path, {})
        discarded = load_json(dis_path, [])
        done = len(entries) + len(discarded)
        shard_size = shard_sizes.get(wid, "?")
        remaining = (shard_size - done) if isinstance(shard_size, int) else "?"
        pct = f"{100*done/shard_size:.1f}%" if isinstance(shard_size, int) and shard_size > 0 else "?"

        status = "✓ complete" if remaining == 0 else "  in progress" if done > 0 else "  not started"
        logger.info(
            f"  W{wid}: {done}/{shard_size} done ({pct}) | "
            f"valid: {len(entries)} | discarded: {len(discarded)} | remaining: {remaining}  {status}"
        )

        if isinstance(shard_size, int):
            grand_shard += shard_size
        grand_valid += len(entries)
        grand_discarded += len(discarded)

    grand_done = grand_valid + grand_discarded
    grand_pct = f"{100*grand_done/grand_shard:.1f}%" if grand_shard > 0 else "?"
    logger.info("─" * 60)
    logger.info(
        f"  Total: {grand_done}/{grand_shard} done ({grand_pct}) | "
        f"valid: {grand_valid} | discarded: {grand_discarded}"
    )

    missing_workers = [wid for wid in range(total_workers) if wid not in worker_ids]
    if missing_workers:
        logger.warning(
            f"Workers with no output files: {missing_workers} — their shard will be absent from the merge. "
            f"Re-run those workers or pass --total-workers to suppress if intentional."
        )

    if args.status or args.dry_run:
        if args.dry_run:
            logger.info("[DRY RUN] Imports OK. Would merge workers into output files. No writes.")
        return

    if missing_workers:
        logger.error(
            f"Refusing to merge: {len(missing_workers)} worker(s) missing output ({missing_workers}). "
            f"Run them first or remove --total-workers to merge only present workers."
        )
        sys.exit(1)

    # ── Merge ────────────────────────────────────────────────────────────────
    logger.info("Merging worker outputs...")

    merged_entries: dict = {}
    merged_discarded: list = []
    seen_discarded: set = set()
    duplicates = 0

    for wid in sorted(worker_ids):
        entries_path = os.path.join(args.workers_dir, f"worker_{wid}", "entries.json")
        dis_path = os.path.join(args.workers_dir, f"worker_{wid}", "discarded.json")

        entries = load_json(entries_path, {})
        discarded = load_json(dis_path, [])

        for word, entry_list in entries.items():
            if word in merged_entries:
                logger.warning(f"Duplicate word '{word}' in W{wid} — keeping first occurrence")
                duplicates += 1
            else:
                merged_entries[word] = entry_list

        for item in discarded:
            word = item.get("word", "")
            if word not in seen_discarded:
                merged_discarded.append(item)
                seen_discarded.add(word)

    save_json(args.out, merged_entries)
    save_json(args.discarded_out, merged_discarded)

    logger.info(f"Merged — valid entries: {len(merged_entries)} → {args.out}")
    logger.info(f"Merged — discarded words: {len(merged_discarded)} → {args.discarded_out}")
    if duplicates:
        logger.warning(f"Duplicate words across workers: {duplicates} (first occurrence kept)")


if __name__ == "__main__":
    main()
