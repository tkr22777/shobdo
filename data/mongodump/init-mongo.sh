#!/bin/bash
# Runs automatically on first container start via /docker-entrypoint-initdb.d/
# MongoDB's entrypoint only executes these scripts when /data/db is empty,
# so this restore is naturally idempotent across container restarts.
set -e

echo "Restoring Shobdo dictionary dump..."
mongorestore --archive=/mongoshobdodump/Dictionary.gz --gzip --drop
echo "Restore complete."
