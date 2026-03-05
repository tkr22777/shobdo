#!/bin/bash
# Usage:
#   ./dump.sh                        # snapshot only, timestamped
#   ./dump.sh pre-missing-words      # snapshot with label
#   ./dump.sh post-upload --update-seed  # snapshot + update committed Dictionary/

set -e

LABEL=${1:-""}
UPDATE_SEED=${2:-""}
TIMESTAMP=$(date +"%Y-%m-%d_%H-%M")
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SNAPSHOTS_DIR="$SCRIPT_DIR/snapshots"

# Build snapshot dir name
if [ -n "$LABEL" ]; then
    SNAPSHOT_NAME="${TIMESTAMP}_${LABEL}"
else
    SNAPSHOT_NAME="${TIMESTAMP}"
fi

SNAPSHOT_PATH="$SNAPSHOTS_DIR/$SNAPSHOT_NAME"
mkdir -p "$SNAPSHOT_PATH"

MONGO_CONTAINER="${MONGO_CONTAINER:-deploy-mongo-1}"
SEED_ARCHIVE="$SCRIPT_DIR/Dictionary.gz"

echo "Dumping to: $SNAPSHOT_PATH"
docker exec "$MONGO_CONTAINER" mongodump -d Dictionary --archive=/tmp/mongodump_tmp.gz --gzip
docker cp "$MONGO_CONTAINER":/tmp/mongodump_tmp.gz "$SNAPSHOT_PATH/Dictionary.gz"
docker exec "$MONGO_CONTAINER" rm -f /tmp/mongodump_tmp.gz
echo "Snapshot saved: $SNAPSHOT_PATH/Dictionary.gz"

# Optionally update the committed seed (Dictionary.gz)
if [ "$UPDATE_SEED" = "--update-seed" ]; then
    echo "Updating committed seed at $SEED_ARCHIVE ..."
    cp "$SNAPSHOT_PATH/Dictionary.gz" "$SEED_ARCHIVE"
    echo "Seed updated: $SEED_ARCHIVE"
fi
