# MongoDB Data Management

## Directory layout

```
data/mongodump/
├── Dictionary.gz        — committed seed: restored automatically on first Docker start
├── init-mongo.sh        — Docker entrypoint script (mounted by docker-compose)
├── README.md            — this file
└── snapshots/           — point-in-time snapshots (not committed to git)
    ├── <timestamp>_<label>/
    │   ├── Dictionary.gz
    │   └── meta.json
    └── ...
```

---

## How the committed seed works

`docker-compose.yml` mounts two paths into the `mongo` container:

```yaml
volumes:
  - "../data/mongodump:/mongoshobdodump"
  - "../data/mongodump/init-mongo.sh:/docker-entrypoint-initdb.d/init-mongo.sh:ro"
```

MongoDB's Docker entrypoint runs every script in `/docker-entrypoint-initdb.d/`
**only when `/data/db` is empty** (i.e. on the very first start of a fresh volume).
`init-mongo.sh` restores `Dictionary.gz` into the `Dictionary` database, so a fresh
local environment is automatically seeded with the full dictionary without any manual
steps.

**This means:** if you `docker-compose down` and back up, the restore does NOT re-run
because `/data/db` already has data. To force a fresh seed, remove the Docker volume
first (`docker volume rm <volume>` or `docker-compose down -v`).

---

## Snapshots

Snapshots are point-in-time `.gz` archives created and managed by the Python
`SnapshotStore` (at `scripts/python_enrichment/src/store/snapshot.py`).

### Create a snapshot

```bash
cd scripts/python_enrichment

# Default label
poetry run python src/store/snapshot.py --label pre-cleanup

# List all snapshots
poetry run python src/store/snapshot.py --list
```

Snapshots are saved to `data/mongodump/snapshots/<timestamp>_<label>/Dictionary.gz`
alongside a `meta.json` with label, timestamp, db name, and file size.

Snapshots are **gitignored** — they live on disk only.

### Restore a snapshot

```bash
# Restore a specific snapshot into the running local container
poetry run python src/store/snapshot.py --restore 2026-03-20_15-55-04_post-foreign-script-cleanup

# Or use the shell script directly (bypasses Python):
./data/mongodump/restore.sh   # ← does not exist; use SnapshotStore
```

---

## Updating the committed seed

After a significant data change (bulk import, cleanup run, etc.), update
`Dictionary.gz` so the next fresh Docker start seeds the correct data:

```bash
# 1. Take a snapshot of the current local DB
cd scripts/python_enrichment
poetry run python src/store/snapshot.py --label <descriptive-label>

# 2. Copy the new snapshot over the committed seed
cp data/mongodump/snapshots/<snapshot-id>/Dictionary.gz data/mongodump/Dictionary.gz

# 3. Commit Dictionary.gz to git
git add data/mongodump/Dictionary.gz
git commit -m "data: update committed seed — <reason>"
```

---

## Syncing to Atlas (production)

Atlas is the production MongoDB. After verifying local changes, sync by restoring
the current snapshot directly into Atlas via `mongorestore`:

```bash
ATLAS_URI="mongodb+srv://<user>:<password>@shobdocluster.vvlqhjq.mongodb.net"

mongorestore \
  --uri "$ATLAS_URI" \
  --archive=data/mongodump/Dictionary.gz \
  --gzip \
  --drop
```

> **Warning:** `--drop` removes the existing Atlas collection before restoring.
> Always verify local state first. The current committed seed is the source of truth.

---

## Current state

| Snapshot | Words | Date | Notes |
|----------|-------|------|-------|
| `Dictionary.gz` (seed) | 107,908 | 2026-03-20 | Post foreign-script cleanup |
| `snapshots/2026-03-20_15-55-04_post-foreign-script-cleanup` | 107,908 | 2026-03-20 | Source of current seed |
| `snapshots/atlas_post_upload.gz` | 107,943 | 2026-03-05 | Pre-cleanup reference |
