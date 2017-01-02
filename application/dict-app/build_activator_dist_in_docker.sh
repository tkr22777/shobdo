#!/bin/bash

#Removing previous image
docker rm -f play_build_instance

#Building the app binaries for distribution
docker run -v $PWD:/dict-app/code --name play_build_instance -it play_build /dict-app/code/build_activator_dist_local.sh
