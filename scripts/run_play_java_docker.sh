#!/bin/bash
docker rm -f play_java
docker run --name play_java -dit -p 127.0.0.1:9000:9000 -v /home/tahsin/Work/play_java_docker_mount_dir:/play_java_docker_mount_dir docker.io/centos:latest


