.PHONY : 
#The following vars are a copy of application/shobdo-app/Makefile
SUBNET="172.10.0.0/16"
NETWORK_NAME="shobdo_net"
CLIENT_IP="172.10.0.2"

start-docker-compose:
	docker-compose -f deploy/docker-compose.yml up -d

stop-docker-compose:
	docker-compose -f deploy/docker-compose.yml down

spin-up-client-container:
	-docker rm -f shobdo-client-instance
	-docker network create --subnet=${SUBNET} ${NETWORK_NAME}
	docker run --net ${NETWORK_NAME} --ip ${CLIENT_IP} -p 32779:80 -v $(PWD)/client/nginx_local.conf:/etc/nginx/nginx.conf -v $(PWD)/client/html/public/:/usr/share/nginx/html/ --name shobdo-client-instance -i -t nginx

