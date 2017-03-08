#!/bin/bash
echo "Root Output:"
curl http://"$(dinghy ip)":9000
echo ""
curl -H "Content-Type: application/json" -X POST -d '{"searchString":"ক"}' http://"$(dinghy ip)":9000/api/v1/search 


