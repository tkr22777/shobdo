#!/bin/bash

#Removing already running instance if any
docker rm -f shobdo-instance

#Deploying the container
docker run -p 9000:9000 --name shobdo-instance -i -t stupefied/shobdo

