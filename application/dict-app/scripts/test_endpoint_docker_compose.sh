#!/bin/bash
echo "Root Output:"
curl -H "Content-Type: application/json" -X POST -d '{"searchString":"ক"}' http://localhost:32779/api/v1/words/search 
