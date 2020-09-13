.PHONY : 

start-containers:
	docker-compose -f deploy/docker-compose.yml up -d

stop-containers:
	docker-compose -f deploy/docker-compose.yml down

run-client: 
	#TODO:BROKEN, FIX LATER
	#Removing already running instance if any
	-docker rm -f shobdo-client-instance
	#Deploying the container
	docker run -p 32779:80 -v $(PWD)/client/nginx.conf:/etc/nginx/nginx.conf -v $(PWD)/client/html/public/:/usr/share/nginx/html/ --name shobdo-client-instance -i -t nginx

