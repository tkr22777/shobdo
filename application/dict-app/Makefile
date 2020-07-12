DOCKER_IMAGE_NAME="dict-app"

build-docker-image:
	echo "Building docker image for the project"
	sbt dist
	docker build --no-cache=true -t ${DOCKER_IMAGE_NAME} .

