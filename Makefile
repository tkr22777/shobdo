.PHONY :
#The following vars are a copy of application/shobdo-app/Makefile
SUBNET="172.10.0.0/16"
NETWORK_NAME="shobdo_net"
CLIENT_IP="172.10.0.2"

# Generate client/html/public/env.js from deploy/local.env.
# This file is gitignored; run this before starting any client container.
generate-client-env:
	@if [ ! -f deploy/local.env ]; then \
		echo "ERROR: deploy/local.env not found."; \
		echo "       Copy deploy/local.env.template to deploy/local.env and fill in your values."; \
		exit 1; \
	fi
	$(eval GOOGLE_CLIENT_ID=$(shell grep --color=never '^SHOBDO_GOOGLE_CLIENT_ID=' deploy/local.env | cut -d'=' -f2-))
	@echo "window.GOOGLE_CLIENT_ID = '$(GOOGLE_CLIENT_ID)';" > client/html/public/env.js
	@echo "Generated client/html/public/env.js"

# Generate client/react/.env.local from deploy/local.env.
# Consumed by Vite as import.meta.env.VITE_GOOGLE_CLIENT_ID.
generate-react-env:
	@if [ ! -f deploy/local.env ]; then \
		echo "ERROR: deploy/local.env not found."; \
		echo "       Copy deploy/local.env.template to deploy/local.env and fill in your values."; \
		exit 1; \
	fi
	$(eval GOOGLE_CLIENT_ID=$(shell grep --color=never '^SHOBDO_GOOGLE_CLIENT_ID=' deploy/local.env | cut -d'=' -f2-))
	@echo "VITE_GOOGLE_CLIENT_ID=$(GOOGLE_CLIENT_ID)" > client/react/.env.local
	@echo "Generated client/react/.env.local"

# Generate sitemap.xml from the MongoDB BSON dump into client/react/public/sitemap.xml
generate-sitemap:
	node scripts/generate-sitemap.mjs

# Install React dependencies and build the production bundle into client/react/dist/
build-react: generate-react-env generate-sitemap
	npm --prefix client/react install
	npm --prefix client/react run build

start-docker-compose: build-react
	docker-compose -f deploy/docker-compose.yml up -d

stop-docker-compose:
	docker-compose -f deploy/docker-compose.yml down

spin-up-client-container-for-local: generate-client-env
	-docker rm -f shobdo-client-instance
	-docker network create --subnet=${SUBNET} ${NETWORK_NAME}
	docker run --net ${NETWORK_NAME} --ip ${CLIENT_IP} -p 32779:80 -v $(PWD)/client/nginx_local.conf:/etc/nginx/nginx.conf -v $(PWD)/client/html/public/:/usr/share/nginx/html/ --name shobdo-client-instance -i -t nginx

spin-up-client-container-for-render: generate-client-env
	-docker rm -f shobdo-client-instance
	-docker network create --subnet=${SUBNET} ${NETWORK_NAME}
	docker run --net ${NETWORK_NAME} --ip ${CLIENT_IP} -p 32779:80 -v $(PWD)/client/nginx_render.conf:/etc/nginx/nginx.conf -v $(PWD)/client/html/public/:/usr/share/nginx/html/ --name shobdo-client-instance -i -t nginx
