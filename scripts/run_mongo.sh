#!/bin/bash

#Removing already running instance if any
docker rm -f mongo-instance

#Deploying the container
docker run -p 27017:27017 --name mongo-instance -i -t mongo

