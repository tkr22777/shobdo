#!/bin/bash
# Usage:
#   ./restore.sh                                           # restore from committed seed (Dictionary/)
#   ./restore.sh snapshots/2026-03-04_pre-missing-words   # restore from a snapshot directory
#   ./restore.sh snapshots/atlas_post_upload.gz            # restore from a gzip archive

set -e

MONGO_CONTAINER="${MONGO_CONTAINER:-deploy-mongo-1}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
TARGET=${1:-"$SCRIPT_DIR/Dictionary"}

# If a relative path is given, resolve relative to script dir
if [[ "$TARGET" != /* ]]; then
    TARGET="$SCRIPT_DIR/$TARGET"
fi

if [[ "$TARGET" == *.gz ]]; then
    # Gzip archive restore
    if [ ! -f "$TARGET" ]; then
        echo "Error: archive not found: $TARGET"
        exit 1
    fi
    echo "Restoring from archive: $TARGET"
    docker cp "$TARGET" "$MONGO_CONTAINER":/tmp/restore_archive.gz
    docker exec "$MONGO_CONTAINER" mongorestore --archive=/tmp/restore_archive.gz --gzip --drop
    docker exec "$MONGO_CONTAINER" rm /tmp/restore_archive.gz
else
    # Directory restore
    if [ ! -d "$TARGET" ]; then
        echo "Error: snapshot directory not found: $TARGET"
        exit 1
    fi
    echo "Restoring from directory: $TARGET"
    docker cp "$TARGET/Dictionary" "$MONGO_CONTAINER":/tmp/restore_dir
    docker exec "$MONGO_CONTAINER" mongorestore --db Dictionary /tmp/restore_dir --drop
    docker exec "$MONGO_CONTAINER" rm -rf /tmp/restore_dir
fi

echo "Restore complete."
