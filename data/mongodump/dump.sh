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

echo "Dumping to: $SNAPSHOT_PATH"
docker exec "$MONGO_CONTAINER" mongodump -d Dictionary -o /tmp/mongodump_tmp
docker cp "$MONGO_CONTAINER":/tmp/mongodump_tmp/Dictionary "$SNAPSHOT_PATH/Dictionary"
docker exec "$MONGO_CONTAINER" rm -rf /tmp/mongodump_tmp
echo "Snapshot saved: $SNAPSHOT_PATH"

# Optionally update the committed seed (Dictionary/)
if [ "$UPDATE_SEED" = "--update-seed" ]; then
    echo "Updating committed seed at $SCRIPT_DIR/Dictionary/ ..."
    rm -rf "$SCRIPT_DIR/Dictionary"
    cp -r "$SNAPSHOT_PATH/Dictionary" "$SCRIPT_DIR/Dictionary"
    echo "Seed updated."
fi
