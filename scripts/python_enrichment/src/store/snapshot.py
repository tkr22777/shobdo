"""
snapshot.py — SnapshotStore: create, list, and restore MongoDB snapshots.

Each snapshot is a gzip archive produced by ``mongodump`` inside a running
Docker container and stored on the host at::

    data/mongodump/snapshots/<timestamp>[_<label>]/Dictionary.gz

A ``meta.json`` file sits alongside the archive and records label, timestamp,
database name, and size so the store can list and identify snapshots without
inspecting filenames.

Usage:
    from src.store.snapshot import SnapshotStore

    store = SnapshotStore()

    # create
    snap = store.create(label="pre-cleanup")
    print(snap["id"], snap["path"])

    # list
    for s in store.list():
        print(s["id"], s["label"], s["size_bytes"])

    # restore
    store.restore(snap["id"])

    # get the most recent snapshot
    latest = store.latest()

CLI:
    poetry run python src/store/snapshot.py --label pre-cleanup
    poetry run python src/store/snapshot.py --list
    poetry run python src/store/snapshot.py --restore 2026-03-20_14-05_pre-cleanup
"""

from __future__ import annotations

import argparse
import json
import subprocess
import sys
from datetime import datetime
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent.parent.parent))

from src.logger import setup_logger

logger = setup_logger("store.snapshot")

REPO_ROOT = Path(__file__).resolve().parents[4]
SNAPSHOTS_DIR = REPO_ROOT / "data" / "mongodump" / "snapshots"

_TMP_PATH_IN_CONTAINER = "/tmp/shobdo_snapshot.gz"


# ---------------------------------------------------------------------------
# SnapshotMeta TypedDict (plain dict in practice — no runtime validation)
# ---------------------------------------------------------------------------

# {
#   "id":         "2026-03-20_14-05_pre-cleanup",
#   "path":       "/abs/path/to/Dictionary.gz",
#   "label":      "pre-cleanup",
#   "db":         "Dictionary",
#   "created_at": "2026-03-20T14:05:32",
#   "size_bytes": 23456789,
# }


class SnapshotStore:
    """
    Manages MongoDB snapshots stored as gzip files on the host filesystem.

    Parameters
    ----------
    snapshots_dir : Path | str
        Root directory where snapshot subdirectories are stored.
        Defaults to ``data/mongodump/snapshots/`` under the repo root.
    container : str
        Name of the Docker container running MongoDB.
    db : str
        Name of the MongoDB database to dump / restore.
    """

    def __init__(
        self,
        snapshots_dir: Path | str | None = None,
        container: str = "deploy-mongo-1",
        db: str = "Dictionary",
    ):
        self.snapshots_dir = Path(snapshots_dir) if snapshots_dir else SNAPSHOTS_DIR
        self.container = container
        self.db = db

    # ------------------------------------------------------------------
    # create
    # ------------------------------------------------------------------

    def create(self, label: str = "") -> dict:
        """
        Dump the database from the running container and save to disk.

        Returns a SnapshotMeta dict.
        Raises RuntimeError if the dump or copy fails.
        """
        self.snapshots_dir.mkdir(parents=True, exist_ok=True)

        now = datetime.now()
        timestamp = now.strftime("%Y-%m-%d_%H-%M-%S")
        dir_name = f"{timestamp}_{label}" if label else timestamp
        snapshot_dir = self.snapshots_dir / dir_name
        snapshot_dir.mkdir(parents=True, exist_ok=True)
        archive_path = snapshot_dir / "Dictionary.gz"
        meta_path = snapshot_dir / "meta.json"

        logger.info(f"Creating snapshot from container '{self.container}', db '{self.db}' ...")

        # Step 1: mongodump inside the container → temp archive
        self._run([
            "docker", "exec", self.container,
            "mongodump",
            "-d", self.db,
            f"--archive={_TMP_PATH_IN_CONTAINER}",
            "--gzip",
        ], "mongodump")

        # Step 2: copy archive out of the container
        self._run(
            ["docker", "cp", f"{self.container}:{_TMP_PATH_IN_CONTAINER}", str(archive_path)],
            "docker cp",
        )

        size = archive_path.stat().st_size
        meta = {
            "id": dir_name,
            "path": str(archive_path),
            "label": label,
            "db": self.db,
            "created_at": now.isoformat(timespec="seconds"),
            "size_bytes": size,
        }
        meta_path.write_text(json.dumps(meta, ensure_ascii=False, indent=2))

        logger.info(f"Snapshot saved → {archive_path} ({size // 1024} KB)")
        return meta

    # ------------------------------------------------------------------
    # list
    # ------------------------------------------------------------------

    def list(self) -> list[dict]:
        """
        Return all snapshots sorted by creation time (newest first).

        Reads ``meta.json`` from each snapshot directory; falls back to
        inferring metadata from the directory name if meta.json is absent.
        """
        if not self.snapshots_dir.exists():
            return []

        metas = []
        for d in sorted(self.snapshots_dir.iterdir(), reverse=True):
            if not d.is_dir():
                continue
            archive = d / "Dictionary.gz"
            if not archive.exists():
                continue
            meta_file = d / "meta.json"
            if meta_file.exists():
                try:
                    meta = json.loads(meta_file.read_text())
                    metas.append(meta)
                    continue
                except json.JSONDecodeError:
                    pass
            # Fallback: synthesise from directory name
            metas.append({
                "id": d.name,
                "path": str(archive),
                "label": "",
                "db": self.db,
                "created_at": d.name[:16].replace("_", "T").replace("-", ":", 2),
                "size_bytes": archive.stat().st_size,
            })
        return metas

    # ------------------------------------------------------------------
    # restore
    # ------------------------------------------------------------------

    def restore(self, snapshot_id: str) -> None:
        """
        Restore the database from a snapshot by ID.

        Drops the existing database inside the container and replaces it
        with the contents of the archive.

        Raises:
            FileNotFoundError if the snapshot archive does not exist.
            RuntimeError if docker cp or mongorestore fails.
        """
        archive_path = self.snapshots_dir / snapshot_id / "Dictionary.gz"
        if not archive_path.exists():
            raise FileNotFoundError(f"Snapshot archive not found: {archive_path}")

        logger.info(f"Restoring snapshot '{snapshot_id}' into container '{self.container}' ...")

        # Step 1: copy archive into the container
        self._run(
            ["docker", "cp", str(archive_path), f"{self.container}:{_TMP_PATH_IN_CONTAINER}"],
            "docker cp (to container)",
        )

        # Step 2: mongorestore — drop + replace
        self._run([
            "docker", "exec", self.container,
            "mongorestore",
            "--drop",
            "-d", self.db,
            f"--archive={_TMP_PATH_IN_CONTAINER}",
            "--gzip",
        ], "mongorestore")

        logger.info(f"Restore complete from snapshot '{snapshot_id}'")

    # ------------------------------------------------------------------
    # convenience
    # ------------------------------------------------------------------

    def latest(self) -> dict | None:
        """Return the most recently created snapshot, or None if there are none."""
        snaps = self.list()
        return snaps[0] if snaps else None

    def get(self, snapshot_id: str) -> dict | None:
        """Return the SnapshotMeta for *snapshot_id*, or None if not found."""
        for s in self.list():
            if s["id"] == snapshot_id:
                return s
        return None

    # ------------------------------------------------------------------
    # internal
    # ------------------------------------------------------------------

    def _run(self, cmd: list[str], step: str) -> None:
        result = subprocess.run(cmd, capture_output=True, text=True)
        if result.returncode != 0:
            raise RuntimeError(
                f"{step} failed (exit {result.returncode}):\n{result.stderr.strip()}"
            )


# ---------------------------------------------------------------------------
# CLI
# ---------------------------------------------------------------------------

def main():
    parser = argparse.ArgumentParser(description="MongoDB snapshot management")
    parser.add_argument("--container", default="deploy-mongo-1")
    parser.add_argument("--db",        default="Dictionary")
    parser.add_argument("--label",     default="", help="Label for new snapshot")

    group = parser.add_mutually_exclusive_group()
    group.add_argument("--list",    action="store_true", help="List all snapshots")
    group.add_argument("--restore", metavar="SNAPSHOT_ID", help="Restore a snapshot by ID")

    args = parser.parse_args()
    store = SnapshotStore(container=args.container, db=args.db)

    if args.list:
        snaps = store.list()
        if not snaps:
            print("No snapshots found.")
            return
        print(f"{'ID':<40} {'label':<20} {'size':>10}  created_at")
        print("-" * 90)
        for s in snaps:
            size_kb = s["size_bytes"] // 1024
            print(f"{s['id']:<40} {s['label']:<20} {size_kb:>8} KB  {s['created_at']}")
        return

    if args.restore:
        try:
            store.restore(args.restore)
        except (FileNotFoundError, RuntimeError) as e:
            logger.error(str(e))
            sys.exit(1)
        return

    # Default: create
    try:
        snap = store.create(label=args.label)
        print(f"Snapshot created: {snap['id']}")
    except RuntimeError as e:
        logger.error(str(e))
        sys.exit(1)


if __name__ == "__main__":
    main()
