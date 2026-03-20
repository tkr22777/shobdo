"""
store — database versioning: snapshots + transactional rollback.

    from src.store.snapshot import SnapshotStore
    from src.store.transaction import WordTransaction

    # --- standalone snapshot ---
    store = SnapshotStore()
    snap  = store.create(label="before-cleanup")  # returns SnapshotMeta
    store.restore(snap["id"])                      # roll back to that state

    # --- transactional block ---
    with WordTransaction(label="bulk-fix") as tx:
        tx.client.update_meaning(word_id, mid, {"text": "fixed"})
        tx.client.delete_meaning(other_id, bad_mid)
    # success → optional post-tx checkpoint created automatically
    # exception → pre-tx snapshot is restored, exception re-raised
"""
