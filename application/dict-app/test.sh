#!/bin/bash
echo "Root Output:"
curl http://localhost:9000
echo ""
echo "Dict Post Output:"
curl -X POST http://localhost:9000/dict/Stumble/ToFall
echo ""
echo "Dict Output:"
curl http://localhost:9000/dict/Stumble
echo ""
