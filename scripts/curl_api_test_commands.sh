#!/bin/bash

#Splash screen
IP="192.168.99.100"
PORT="9000"
echo "curl sanity tests:"

echo "Testing GET home route:"
curl -X GET http://$IP:$PORT/api/v1
echo ""

#Testing POST route
echo "Testing POST route:"
curl -H "Content-Type: application/json" -X POST -d '{"name":"SIN"}' http://$IP:$PORT/api/v1/posttest
echo ""

#Generating a test dictionary
echo "Testing Temp Dictionary Generate:"
curl -H "Content-Type: application/json" -X POST -d '{"wordCount":"100"}' http://$IP:$PORT/api/v1/generate
echo ""

#Serch word by spelling in the dictionary
echo "Testing Search POST:"
curl -H "Content-Type: application/json" -X POST -d '{"searchString":"ঙ"}' http://$IP:$PORT/api/v1/search
echo ""

#Get word by spelling in the dictionary
#echo "Testing get word POST:"
curl -H "Content-Type: application/json" -X POST -d '{"wordSpelling":"ঙ"}' http://$IP:$PORT/api/v1/word
#echo ""
