#!/bin/bash

#Removing already running instance if any
docker rm -f dict-app-instance

#Deploying the container
docker run -p 9000:9000 --name dict-app-instance -i -t dict-app

