#!/bin/bash

#Removing already running instance if any
docker rm -f redis-instance

#Deploying the container
docker run -p 6379:6379 --name redis-instance -i -t redis

