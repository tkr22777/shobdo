"""
batch_processor.py — Classify all Words in MongoDB in batches using AI.

Reads every ACTIVE word from MongoDB, sends them to the AI classifier in
configurable batches, writes results to data/cleanup/results/<run_dir>/.
Progress is saved after every batch so the run is fully resumable.

Output files (in data/cleanup/results/<run_dir>/):
  results.json     — all classifications so far (list of ClassificationResult)
  progress.json    — set of word IDs already processed (for resumption)
  meta.json        — run metadata (args, timestamps, counts)

Usage:
    # Dry-run: show what would be done without writing anything
    poetry run python src/cleanup/batch_processor.py --dry-run

    # Real run with default settings (batch 50, all words)
    poetry run python src/cleanup/batch_processor.py

    # Resume an interrupted run
    poetry run python src/cleanup/batch_processor.py --run-dir data/cleanup/results/2026-03-20_14-00

    # Limit for testing
    poetry run python src/cleanup/batch_processor.py --limit 200 --batch-size 25

    # Always snapshot DB before starting
    poetry run python src/cleanup/batch_processor.py --snapshot --snapshot-label pre-cleanup
"""

from __future__ import annotations

import argparse
import json
import sys
from concurrent.futures import ThreadPoolExecutor, as_completed
from datetime import datetime
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent.parent.parent))

from pymongo import MongoClient

from src.cleanup.ai_classifier import ClassificationResult, classify_batch
from src.cleanup.archiver import create_snapshot
from src.logger import setup_logger

logger = setup_logger("batch_processor")

# Paths
SCRIPT_DIR = Path(__file__).resolve().parent
REPO_ROOT = SCRIPT_DIR.parents[3]
CLEANUP_DATA_DIR = REPO_ROOT / "scripts" / "python_enrichment" / "data" / "cleanup"
RESULTS_BASE_DIR = CLEANUP_DATA_DIR / "results"

SAVE_INTERVAL = 5   # save progress every N batches


# ── MongoDB helpers ────────────────────────────────────────────────────────────

def load_words(mongo_uri: str, db_name: str) -> list[dict]:
    """Fetch all ACTIVE words (id + spelling only) from MongoDB."""
    client = MongoClient(mongo_uri)
    collection = client[db_name]["Words"]
    words = [
        {"id": doc["id"], "spelling": doc["spelling"]}
        for doc in collection.find(
            {"status": "ACTIVE"},
            {"id": 1, "spelling": 1, "_id": 0},
        )
    ]
    logger.info(f"Loaded {len(words):,} ACTIVE words from MongoDB")
    return words


# ── Progress persistence ───────────────────────────────────────────────────────

def load_progress(run_dir: Path) -> tuple[list[ClassificationResult], set[str]]:
    """Load existing results and done-IDs from a previous run."""
    results_file = run_dir / "results.json"
    progress_file = run_dir / "progress.json"

    results: list[ClassificationResult] = []
    done_ids: set[str] = set()

    if results_file.exists():
        with open(results_file) as f:
            results = json.load(f)
        logger.info(f"Resumed: {len(results):,} results already saved")

    if progress_file.exists():
        with open(progress_file) as f:
            done_ids = set(json.load(f))

    return results, done_ids


def save_progress(
    run_dir: Path,
    results: list[ClassificationResult],
    done_ids: set[str],
) -> None:
    run_dir.mkdir(parents=True, exist_ok=True)
    with open(run_dir / "results.json", "w", encoding="utf-8") as f:
        json.dump(results, f, ensure_ascii=False, indent=2)
    with open(run_dir / "progress.json", "w") as f:
        json.dump(sorted(done_ids), f)


def save_meta(run_dir: Path, meta: dict) -> None:
    run_dir.mkdir(parents=True, exist_ok=True)
    with open(run_dir / "meta.json", "w", encoding="utf-8") as f:
        json.dump(meta, f, ensure_ascii=False, indent=2)


# ── Main processing loop ───────────────────────────────────────────────────────

def run(
    mongo_uri: str,
    db_name: str,
    batch_size: int,
    run_dir: Path,
    limit: int = 0,
    threads: int = 1,
    dry_run: bool = False,
) -> None:
    words = load_words(mongo_uri, db_name)

    results, done_ids = load_progress(run_dir)

    # Filter out already-processed words
    remaining = [w for w in words if w["id"] not in done_ids]
    if limit > 0:
        remaining = remaining[:limit]

    total = len(remaining)
    logger.info(
        f"Words to classify: {total:,} "
        f"(skipping {len(done_ids):,} already done)"
    )

    if dry_run:
        logger.info(
            f"[DRY RUN] Would process {total:,} words in batches of {batch_size} "
            f"using {threads} thread(s). No changes written."
        )
        return

    # Split into batches
    batches = [remaining[i : i + batch_size] for i in range(0, total, batch_size)]
    logger.info(f"Processing {len(batches)} batches of up to {batch_size} words each")

    meta = {
        "mongo_uri": mongo_uri,
        "db": db_name,
        "batch_size": batch_size,
        "threads": threads,
        "total_words": len(words),
        "resumed_from": len(done_ids),
        "to_process": total,
        "started_at": datetime.utcnow().isoformat(),
        "finished_at": None,
    }
    save_meta(run_dir, meta)

    batches_since_save = 0

    def process_batch(batch_index: int, batch: list[dict]) -> list[ClassificationResult]:
        batch_results = classify_batch(batch)
        logger.info(
            f"Batch {batch_index + 1}/{len(batches)}: "
            f"{len(batch_results)} words classified"
        )
        return batch_results

    if threads > 1:
        with ThreadPoolExecutor(max_workers=threads) as executor:
            futures = {
                executor.submit(process_batch, i, batch): i
                for i, batch in enumerate(batches)
            }
            for future in as_completed(futures):
                batch_results = future.result()
                results.extend(batch_results)
                done_ids.update(r["id"] for r in batch_results)
                batches_since_save += 1
                if batches_since_save >= SAVE_INTERVAL:
                    save_progress(run_dir, results, done_ids)
                    batches_since_save = 0
    else:
        for i, batch in enumerate(batches):
            batch_results = process_batch(i, batch)
            results.extend(batch_results)
            done_ids.update(r["id"] for r in batch_results)
            batches_since_save += 1
            if batches_since_save >= SAVE_INTERVAL:
                save_progress(run_dir, results, done_ids)
                batches_since_save = 0

    # Final save
    save_progress(run_dir, results, done_ids)

    meta["finished_at"] = datetime.utcnow().isoformat()
    meta["total_classified"] = len(results)
    _log_summary(results)
    save_meta(run_dir, meta)

    logger.info(f"Results written to {run_dir}")


def _log_summary(results: list[ClassificationResult]) -> None:
    from collections import Counter
    counts = Counter(r["classification"] for r in results)
    logger.info(
        f"Classification summary — "
        f"VALID_ROOT: {counts.get('VALID_ROOT', 0):,}  "
        f"LIKELY_INFLECTION: {counts.get('LIKELY_INFLECTION', 0):,}  "
        f"GARBAGE: {counts.get('GARBAGE', 0):,}"
    )


# ── CLI ────────────────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(
        description="Classify all Words in MongoDB as VALID_ROOT, LIKELY_INFLECTION, or GARBAGE"
    )
    parser.add_argument(
        "--mongo",
        default="mongodb://localhost:27017",
        help="MongoDB connection URI (default: mongodb://localhost:27017)",
    )
    parser.add_argument(
        "--db",
        default="Dictionary",
        help="MongoDB database name (default: Dictionary)",
    )
    parser.add_argument(
        "--batch-size",
        type=int,
        default=50,
        help="Words per AI classification batch (default: 50)",
    )
    parser.add_argument(
        "--threads",
        type=int,
        default=1,
        help="Parallel threads for AI calls (default: 1; increase when AI is plugged in)",
    )
    parser.add_argument(
        "--limit",
        type=int,
        default=0,
        help="Max words to process this run (0 = all, useful for testing)",
    )
    parser.add_argument(
        "--run-dir",
        default="",
        help=(
            "Directory to read/write progress and results. "
            "If omitted, a timestamped directory is created under data/cleanup/results/. "
            "Pass an existing run-dir to resume."
        ),
    )
    parser.add_argument(
        "--snapshot",
        action="store_true",
        help="Take a MongoDB snapshot before processing (recommended before first run)",
    )
    parser.add_argument(
        "--snapshot-label",
        default="pre-cleanup",
        help="Label for the snapshot directory (default: pre-cleanup)",
    )
    parser.add_argument(
        "--snapshot-container",
        default="deploy-mongo-1",
        help="Docker container name for snapshot (default: deploy-mongo-1)",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Print what would be done without writing anything",
    )
    args = parser.parse_args()

    # Snapshot before processing
    if args.snapshot:
        if args.dry_run:
            logger.info(
                f"[DRY RUN] Would snapshot container '{args.snapshot_container}' "
                f"with label '{args.snapshot_label}'"
            )
        else:
            try:
                create_snapshot(
                    container=args.snapshot_container,
                    db=args.db,
                    label=args.snapshot_label,
                )
            except RuntimeError as e:
                logger.error(f"Snapshot failed: {e}")
                sys.exit(1)

    # Resolve run directory
    if args.run_dir:
        run_dir = Path(args.run_dir)
    else:
        timestamp = datetime.utcnow().strftime("%Y-%m-%d_%H-%M")
        run_dir = RESULTS_BASE_DIR / timestamp

    logger.info(f"Run directory: {run_dir}")

    run(
        mongo_uri=args.mongo,
        db_name=args.db,
        batch_size=args.batch_size,
        run_dir=run_dir,
        limit=args.limit,
        threads=args.threads,
        dry_run=args.dry_run,
    )


if __name__ == "__main__":
    main()
