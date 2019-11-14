#!/bin/bash
echo "Root Output:"
curl -H "Content-Type: application/json" -X POST -d '{"wordCount": "50" }' http://localhost:32779/api/v1/generate



