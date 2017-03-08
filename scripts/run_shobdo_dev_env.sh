#!/bin/bash

#Removing previous image
docker rm -f centos-java8-activator_devinstance

#Building the app binaries for distribution
docker run -p 9000:9000 -v $PWD/../:/dict-app/code --name centos-java8-activator_devinstance -it stupefied/centos-java8-activator:cached
