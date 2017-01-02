###########################################
# Dockerfile to build the dictionary app
# Based on Centos
###########################################

FROM centos

MAINTAINER Tahsin Kabir

RUN yum update -y

#The home directory for the app
RUN mkdir /dict-app

#The log directory
RUN mkdir /dict-app/logs

#The dependecty directory
RUN mkdir /dict-app/dependency

WORKDIR /dict-app

#Installing Java 8
RUN yum install wget -y

RUN wget -P /dict-app/dependency --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u92-b14/jdk-8u92-linux-x64.rpm

RUN rpm -ivh /dict-app/dependency/jdk-8u92-linux-x64.rpm

#Installing upzip
RUN yum install unzip -y

#The copy the binary from the local target dir!
ADD ./target/universal /dict-app/bin

RUN unzip bin/dict-app-0.11-SNAPSHOT.zip -d bin/

RUN chmod +x bin/dict-app-0.11-SNAPSHOT/bin/dict-app

EXPOSE 9000

CMD bin/dict-app-0.11-SNAPSHOT/bin/dict-app -Dplay.crypto.secret=testtahsin

#ENTRYPOINT usr/bin/mongod
