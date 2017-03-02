########################################
# Dockerfile to build the dictionary app
# You need the centos-java image or 
# build the centos-java image from the 
# docker directory
#########################################

FROM centos

MAINTAINER Tahsin Kabir

RUN curl --silent --location https://rpm.nodesource.com/setup_6.x | bash -

RUN yum install -y nodejs

#Copy the source from the local target dir, we want an independent, self-sufficient image to run
ADD . /client

WORKDIR /client

RUN npm i

EXPOSE 3000

CMD node ./node_modules/react-scripts/bin/react-scripts.js start

