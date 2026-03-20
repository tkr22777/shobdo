"""
archiver.py — CLI wrapper around src.store.snapshot.SnapshotStore.

Kept for backwards compatibility. Prefer using SnapshotStore directly
or the snapshot.py CLI:

    poetry run python src/store/snapshot.py --label pre-cleanup
    poetry run python src/store/snapshot.py --list

This script is equivalent to:

    poetry run python src/cleanup/archiver.py --label pre-cleanup
"""

import argparse
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent.parent.parent))

from src.logger import setup_logger
from src.store.snapshot import SnapshotStore

logger = setup_logger("archiver")


def create_snapshot(
    container: str = "deploy-mongo-1",
    db: str = "Dictionary",
    label: str = "",
) -> Path:
    """
    Backwards-compatible wrapper.  Returns the path to the .gz archive.
    """
    store = SnapshotStore(container=container, db=db)
    meta = store.create(label=label)
    return Path(meta["path"])


def main():
    parser = argparse.ArgumentParser(
        description="Snapshot MongoDB state (wrapper for src.store.snapshot)"
    )
    parser.add_argument("--container", default="deploy-mongo-1")
    parser.add_argument("--db",        default="Dictionary")
    parser.add_argument("--label",     default="")
    args = parser.parse_args()

    try:
        path = create_snapshot(container=args.container, db=args.db, label=args.label)
        print(f"Snapshot created: {path}")
    except RuntimeError as e:
        logger.error(str(e))
        sys.exit(1)


if __name__ == "__main__":
    main()
