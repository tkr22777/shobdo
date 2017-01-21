#!/bin/bash

#Splash screen
curl -X GET http://192.168.99.100:9000/dict
echo ""

#Testing post route
curl -H "Content-Type: application/json" -X POST -d '{"name":"SIN"}' http://192.168.99.100:9000/posttest
echo ""

#Generating a test dictionary
curl -H "Content-Type: application/json" -X POST -d '{"wordCount":"100"}' http://192.168.99.100:9000/dict/generate
echo ""

#Serch word by spelling in the dictionary
curl -H "Content-Type: application/json" -X POST -d '{"spelling":"ঙ"}' http://192.168.99.100:9000/dict/search
echo ""

#Get word by spelling in the dictionary
curl -H "Content-Type: application/json" -X POST -d '{"spelling":"ঙ"}' http://192.168.99.100:9000/dict/word
echo ""
