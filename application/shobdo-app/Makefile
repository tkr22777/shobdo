.PHONY : test spin-up-mongodb-container spin-up-redis-container
SUBNET="172.10.0.0/16"
NETWORK_NAME="shobdo_net"
SHOBDO_PORT="9000"
SHOBDO_IP="172.10.0.3"
MONGO_HOST_IP="172.10.0.4"
REDIS_HOST_IP="172.10.0.5"
DOCKER_IMAGE_NAME="shobdo-app"

MAKEFILE_PATH := $(abspath $(lastword $(MAKEFILE_LIST)))
CURRENT_DIRECTORY := $(notdir $(patsubst %/,%,$(dir $(MAKEFILE_PATH))))

clean:
	sbt clean

test: spin-up-mongodb-container spin-up-redis-container
	sbt clean test

run: spin-up-mongodb-container spin-up-redis-container
	sbt clean run

build: spin-up-mongodb-container spin-up-redis-container
	rm -rf target/universal/*
	sbt clean test dist

build-docker-image:
	# Build from pre-built zip file
	docker build --no-cache=true --build-arg BUILD_FROM_SOURCE=false -t ${DOCKER_IMAGE_NAME} .

build-docker-image-from-source:
	# Build from source
	docker build --no-cache=true -t ${DOCKER_IMAGE_NAME} .

exec-shobdo-container-cli:
	docker exec -it shobdo-instance /bin/bash

spin-up-dockerized-service-only:
	-docker network create --subnet=${SUBNET} ${NETWORK_NAME}
	-docker rm -f shobdo-instance #cleanup existing instance
	docker run --net ${NETWORK_NAME} --ip ${SHOBDO_IP} \
		--env SHOBDO_MONGODB_HOSTNAME=${MONGO_HOST_IP} \
		--env SHOBDO_REDIS_HOSTNAME=${REDIS_HOST_IP} \
		-p ${SHOBDO_PORT}:9000 --name shobdo-instance -i -t ${DOCKER_IMAGE_NAME}

spin-up-dockerized-service-clean-slate: spin-up-mongodb-container spin-up-redis-container
	-docker network create --subnet=${SUBNET} ${NETWORK_NAME}
	-docker rm -f shobdo-instance #cleanup existing instance
	docker run --net ${NETWORK_NAME} --ip ${SHOBDO_IP} \
		--env SHOBDO_MONGODB_HOSTNAME=${MONGO_HOST_IP} \
		--env SHOBDO_REDIS_HOSTNAME=${REDIS_HOST_IP} \
		-p ${SHOBDO_PORT}:9000 --name shobdo-instance -i -t ${DOCKER_IMAGE_NAME}

spin-up-mongodb-container:
	-docker network create --subnet=${SUBNET} ${NETWORK_NAME}
	-docker rm -f mongo-instance #cleanup existing instance
	docker run --net ${NETWORK_NAME} --ip ${MONGO_HOST_IP} -p 27017:27017 \
		-v $(PWD)/../../data/mongodump:/mongoshobdodump \
		--name mongo-instance -i -t -d mongo

exec-mongodb-container-cli:
	docker exec -it mongo-instance /bin/bash

spin-up-redis-container:
	-docker network create --subnet=${SUBNET} ${NETWORK_NAME}
	-docker rm -f redis-instance #cleanup existing instance
	docker run --net ${NETWORK_NAME} --ip ${REDIS_HOST_IP} -p 6379:6379 \
		--name redis-instance -i -t -d redis

exec-redis-container-cli:
	docker exec -it redis-instance redis-cli --raw
