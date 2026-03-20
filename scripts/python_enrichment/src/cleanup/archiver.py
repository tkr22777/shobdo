"""
archiver.py — Snapshot the current MongoDB state before a cleanup run.

Creates a timestamped gzip archive in data/mongodump/snapshots/ using
the same mongodump-over-Docker-exec pattern as data/mongodump/dump.sh.

Usage:
    poetry run python src/cleanup/archiver.py
    poetry run python src/cleanup/archiver.py --label pre-cleanup
    poetry run python src/cleanup/archiver.py --container my-mongo --label test
"""

import argparse
import subprocess
import sys
from datetime import datetime
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent.parent.parent))

from src.logger import setup_logger

logger = setup_logger("archiver")

# Paths relative to this repo
REPO_ROOT = Path(__file__).resolve().parents[4]
SNAPSHOTS_DIR = REPO_ROOT / "data" / "mongodump" / "snapshots"


def create_snapshot(
    container: str = "deploy-mongo-1",
    db: str = "Dictionary",
    label: str = "",
) -> Path:
    """
    Dump the given MongoDB database from the running Docker container and
    save the archive to data/mongodump/snapshots/<timestamp>[_<label>]/.

    Returns the path to the saved .gz file.
    Raises RuntimeError on failure.
    """
    SNAPSHOTS_DIR.mkdir(parents=True, exist_ok=True)

    timestamp = datetime.now().strftime("%Y-%m-%d_%H-%M")
    dir_name = f"{timestamp}_{label}" if label else timestamp
    snapshot_dir = SNAPSHOTS_DIR / dir_name
    snapshot_dir.mkdir(parents=True, exist_ok=True)
    dest_file = snapshot_dir / "Dictionary.gz"

    tmp_path = "/tmp/cleanup_snapshot.gz"

    logger.info(f"Creating snapshot from container '{container}', db '{db}'")

    # Step 1: mongodump inside the container
    dump_cmd = [
        "docker", "exec", container,
        "mongodump",
        "-d", db,
        "--archive=" + tmp_path,
        "--gzip",
    ]
    result = subprocess.run(dump_cmd, capture_output=True, text=True)
    if result.returncode != 0:
        raise RuntimeError(
            f"mongodump failed (exit {result.returncode}):\n{result.stderr}"
        )

    # Step 2: copy archive out of the container
    cp_cmd = ["docker", "cp", f"{container}:{tmp_path}", str(dest_file)]
    result = subprocess.run(cp_cmd, capture_output=True, text=True)
    if result.returncode != 0:
        raise RuntimeError(
            f"docker cp failed (exit {result.returncode}):\n{result.stderr}"
        )

    logger.info(f"Snapshot saved → {dest_file} ({dest_file.stat().st_size // 1024} KB)")
    return dest_file


def main():
    parser = argparse.ArgumentParser(
        description="Snapshot MongoDB state to data/mongodump/snapshots/"
    )
    parser.add_argument(
        "--container",
        default="deploy-mongo-1",
        help="Docker container name running MongoDB (default: deploy-mongo-1)",
    )
    parser.add_argument(
        "--db",
        default="Dictionary",
        help="MongoDB database name to dump (default: Dictionary)",
    )
    parser.add_argument(
        "--label",
        default="",
        help="Optional label appended to snapshot directory name (e.g. 'pre-cleanup')",
    )
    args = parser.parse_args()

    try:
        path = create_snapshot(
            container=args.container,
            db=args.db,
            label=args.label,
        )
        print(f"Snapshot created: {path}")
    except RuntimeError as e:
        logger.error(str(e))
        sys.exit(1)


if __name__ == "__main__":
    main()
