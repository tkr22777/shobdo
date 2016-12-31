#!/bin/bash

#Splash screen
curl -X GET http://localhost:9000/dict
echo ""

#Testing post route
curl -H "Content-Type: application/json" -X POST -d '{"name":"SIN"}' http://localhost:9000/temp
echo ""

#Generating a test dictionary
curl -H "Content-Type: application/json" -X POST -d '{"wordCount":"0"}' http://localhost:9000/dict/generate
echo ""

#Serch word by spelling in the dictionary
curl -H "Content-Type: application/json" -X POST -d '{"spelling":"ঙ"}' http://localhost:9000/dict/search
echo ""

#Get word by spelling in the dictionary
curl -H "Content-Type: application/json" -X POST -d '{"spelling":"ঙঘন"}' http://localhost:9000/dict/word
echo ""

