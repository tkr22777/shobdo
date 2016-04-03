#!/bin/bash
docker run --name mysql-bd-db -e MYSQL_ROOT_PASSWORD=testpw -d mysql:latest
