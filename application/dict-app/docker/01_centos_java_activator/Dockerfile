############################################################
# Dockerfile to build the dictionary app binary distribution
# Based on Centos
############################################################

FROM centos-java

MAINTAINER Tahsin Kabir

RUN curl -O https://downloads.typesafe.com/typesafe-activator/1.3.10/typesafe-activator-1.3.10.zip

RUN unzip typesafe-activator-1.3.10.zip -d / && \
    rm typesafe-activator-1.3.10.zip

ENV PATH "$PATH:/activator-dist-1.3.10/bin/"
