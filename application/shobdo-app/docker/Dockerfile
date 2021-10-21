###########################################
# Dockerfile to build the dictionary app
# Based on Centos
###########################################

FROM openjdk:8-jdk-alpine

MAINTAINER Tahsin Kabir

RUN yum update -y
RUN yum install git -y && \
    yum install vim -y && \
    yum install unzip -y && \
    yum install make -y

#The home directory for the app
RUN mkdir /shobdo-app

#The log directory
RUN mkdir /shobdo-app/logs

#The dependency directory
RUN mkdir /shobdo-app/dependency

WORKDIR /shobdo-app

#Installing asdf, java and sbt
RUN git clone https://github.com/asdf-vm/asdf.git ~/.asdf --branch v0.8.1 && \
    echo ". \$HOME/.asdf/asdf.sh" >> ~/.bashrc && \
    source ~/.bashrc && \
    asdf plugin add sbt https://github.com/bram2000/asdf-sbt.git && \
    asdf install sbt 1.5.5 && \
    asdf global sbt 1.5.5
