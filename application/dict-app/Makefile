DOCKER_IMAGE_NAME="dict-app"
SERVICE_PORT="32779"
MAKEFILE_PATH := $(abspath $(lastword $(MAKEFILE_LIST)))
CURRENT_DIRECTOR := $(notdir $(patsubst %/,%,$(dir $(MAKEFILE_PATH))))

clean:
	sbt clean

test: 
	sbt clean test

run: 
	sbt clean run

build:
	rm -rf target/universal/*
	sbt clean test dist

build-docker-image: build
	docker build --no-cache=true -t ${DOCKER_IMAGE_NAME} .

spin-up-dockerized-service:
	-docker rm -f shobdo-instance #removing existing instance, if any
	docker run -p ${SERVICE_PORT}:9000 --name shobdo-instance -i -t ${DOCKER_IMAGE_NAME}

spin-up-mongodb-container:
	-docker rm -f mongo-instance #removing existing instance, if any
	docker run -p 27017:27017 -v $(PWD)/../../data/mongodump:/mongoshobdodump \
		--name mongo-instance -i -t -d mongo

exec-mongodb-container-cli:
	docker exec -it mongo-instance mongo

spin-up-redis-container:
	-docker rm -f redis-instance #removing existing instance, if any
	docker run -p 6379:6379 --name redis-instance -i -t -d redis

exec-redis-container-cli:
	docker exec -it redis-instance redis-cli --raw
