#!/bin/bash

#Removing previous image
docker rm -f centos-java8-activator_instance

#Building the app binaries for distribution
docker run -v $PWD:/dict-app/code -w="/dict-app/code" --name centos-java8-activator_instance -it stupefied/centos-java8-activator bash

#Execute the following command
#docker exec -it centos-java8-activator_instance bash 
#/dict-app/code/build_local.sh
