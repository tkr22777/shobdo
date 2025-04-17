.PHONY: stop-client

NETWORK_NAME="shobdo_net"
SUBNET="172.10.0.0/16"
CLIENT_PORT="8080"
CLIENT_IP="172.10.0.6"
CLIENT_CONTAINER_NAME="shobdo-client"
CLIENT_IMAGE_NAME="shobdo-client-image"

build-client-image:
	docker build --no-cache=true -t ${CLIENT_IMAGE_NAME} .

start-client:
	-docker network create --subnet=${SUBNET} ${NETWORK_NAME}
	-docker rm -f ${CLIENT_CONTAINER_NAME} # cleanup existing instance
	docker run --net ${NETWORK_NAME} --ip ${CLIENT_IP} \
		-p ${CLIENT_PORT}:80 \
		--name ${CLIENT_CONTAINER_NAME} \
		-d ${CLIENT_IMAGE_NAME}
	@echo "Client is running at http://localhost:${CLIENT_PORT}"

stop-client:
	-docker stop ${CLIENT_CONTAINER_NAME}
	-docker rm ${CLIENT_CONTAINER_NAME}
	@echo "Client container stopped and removed"

clean-client-image: stop-client
	-docker rmi ${CLIENT_IMAGE_NAME}
	@echo "Client image removed"

exec-client-container-cli:
	docker exec -it ${CLIENT_CONTAINER_NAME} /bin/sh