"""
transaction.py — WordTransaction: snapshot-before + auto-restore-on-failure.

Combines SnapshotStore and WordClient into a single context manager so that
any batch of writes is either fully applied or fully reverted.

    Lifecycle
    ---------
    __enter__
        1. Take a pre-transaction snapshot (label = "<label>-pre")
        2. Return self so the caller can use ``tx.client``

    block body
        Caller performs arbitrary word CRUD via ``tx.client``

    __exit__ — success path
        Optional: take a post-transaction snapshot (label = "<label>-post")
        if *checkpoint_on_success* is True (default True).

    __exit__ — exception path
        Restore the pre-transaction snapshot, then re-raise the exception.
        The caller sees the original error; the database is back to its
        pre-run state.

Usage
-----
    from src.store.transaction import WordTransaction

    with WordTransaction(label="fix-meanings") as tx:
        word = tx.client.get_by_spelling("শব্দ")
        mid  = next(iter(word["meanings"]))
        tx.client.update_meaning(word["id"], mid, {"text": "সংশোধিত অর্থ"})

    # If anything inside the block raises, the DB is restored automatically.

Skip snapshot (dry-run / unit tests):
    with WordTransaction(label="test", snapshot=False) as tx:
        ...

Use an existing snapshot instead of creating a new one:
    with WordTransaction(label="redo", pre_snapshot_id="2026-03-20_14-05_pre-cleanup") as tx:
        ...
"""

from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent.parent.parent))

from src.logger import setup_logger
from src.store.snapshot import SnapshotStore
from src.word_client.client import WordClient

logger = setup_logger("store.transaction")


class WordTransaction:
    """
    Context manager that wraps a WordClient with snapshot-based rollback.

    Parameters
    ----------
    label : str
        Human-readable label; used as prefix for snapshot names.
    base_url : str
        Shobdo API base URL (default: http://localhost:32779).
    snapshot : bool
        If False, skips all snapshot creation/restore (useful for tests or
        when you know the DB is disposable).
    checkpoint_on_success : bool
        If True (default), create a post-transaction snapshot on clean exit.
    pre_snapshot_id : str | None
        If given, skip creating a new pre-tx snapshot and use this existing
        one for rollback instead.
    store : SnapshotStore | None
        Override the SnapshotStore instance (e.g. for testing or custom dirs).
    """

    def __init__(
        self,
        label: str = "tx",
        base_url: str = "http://localhost:32779",
        snapshot: bool = True,
        checkpoint_on_success: bool = True,
        pre_snapshot_id: str | None = None,
        store: SnapshotStore | None = None,
    ):
        self.label = label
        self.snapshot = snapshot
        self.checkpoint_on_success = checkpoint_on_success
        self.pre_snapshot_id = pre_snapshot_id

        self._store = store or SnapshotStore()
        self.client = WordClient(base_url=base_url)

        # Set by __enter__ so __exit__ knows what to restore
        self._pre_snap: dict | None = None

    # ------------------------------------------------------------------
    # Context manager protocol
    # ------------------------------------------------------------------

    def __enter__(self) -> "WordTransaction":
        try:
            if self.snapshot:
                if self.pre_snapshot_id:
                    # Use a caller-supplied snapshot as the rollback point
                    snap = self._store.get(self.pre_snapshot_id)
                    if snap is None:
                        raise FileNotFoundError(
                            f"Pre-snapshot '{self.pre_snapshot_id}' not found."
                        )
                    self._pre_snap = snap
                    logger.info(f"[{self.label}] Using existing pre-snapshot: {snap['id']}")
                else:
                    self._pre_snap = self._store.create(label=f"{self.label}-pre")
                    logger.info(f"[{self.label}] Pre-snapshot: {self._pre_snap['id']}")
            else:
                logger.info(f"[{self.label}] Snapshot disabled — no rollback available")
        except Exception:
            self.client.close()
            raise
        return self

    def __exit__(self, exc_type, exc_val, exc_tb) -> bool:
        if exc_type is not None:
            # ---- failure path ----
            logger.error(f"[{self.label}] Error during transaction: {exc_val}")
            if self.snapshot and self._pre_snap:
                logger.info(f"[{self.label}] Restoring pre-snapshot '{self._pre_snap['id']}' ...")
                try:
                    self._store.restore(self._pre_snap["id"])
                    logger.info(f"[{self.label}] Restore complete.")
                except Exception as restore_err:
                    logger.error(f"[{self.label}] RESTORE FAILED: {restore_err}")
                    logger.error(
                        f"[{self.label}] Manual restore: "
                        f"src/store/snapshot.py --restore {self._pre_snap['id']}"
                    )
            else:
                logger.warning(f"[{self.label}] No snapshot to restore from.")
            self.client.close()
            return False  # re-raise the original exception

        # ---- success path ----
        if self.snapshot and self.checkpoint_on_success:
            try:
                post_snap = self._store.create(label=f"{self.label}-post")
                logger.info(f"[{self.label}] Post-snapshot: {post_snap['id']}")
            except Exception as snap_err:
                # Don't fail the whole transaction just because post-snapshot failed
                logger.warning(f"[{self.label}] Post-snapshot failed (non-fatal): {snap_err}")

        self.client.close()
        return False

    # ------------------------------------------------------------------
    # Manual checkpoint (call inside the block for mid-transaction saves)
    # ------------------------------------------------------------------

    def checkpoint(self, label: str) -> dict | None:
        """
        Create an intermediate snapshot from inside the transaction block.

        Returns the SnapshotMeta dict, or None if snapshots are disabled.
        Useful for long-running batches where you want intermediate save points.

        Example::

            with WordTransaction(label="big-run") as tx:
                for i, batch in enumerate(batches):
                    process(tx.client, batch)
                    if i % 100 == 0:
                        tx.checkpoint(f"after-batch-{i}")
        """
        if not self.snapshot:
            return None
        snap = self._store.create(label=label)
        logger.info(f"[{self.label}] Checkpoint: {snap['id']}")
        return snap
