#!/bin/bash

#Splash screen
IP="localhost"
PORT="9000"
echo "curl sanity tests:"

echo "Testing GET home route:"
curl -X GET http://localhost:9000/api/v1
echo ""

#Testing POST route
echo "Testing POST route:"
curl -H "Content-Type: application/json" -X POST -d '{"name":"SIN"}' http://localhost:9000/api/v1/posttest
echo ""

#Generating a test dictionary
echo "Testing Temp Dictionary Generate:"
curl -H "Content-Type: application/json" -X POST -d '{"wordCount":"100"}' http://localhost:9000/api/v1/generate
echo ""

#Serch word by spelling in the dictionary
echo "Testing Search POST:"
curl -H "Content-Type: application/json" -X POST -d '{"searchString":"ঙ"}' http://localhost:9000/api/v1/words/search
echo ""

#Get word by spelling in the dictionary
#echo "Testing get word POST:"
curl -H "Content-Type: application/json" -X POST -d '{"wordSpelling":"ঙ"}' http://localhost:9000/api/v1/words/postget
#echo ""
