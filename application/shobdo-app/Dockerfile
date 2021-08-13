########################################
# Dockerfile to build the dictionary app
# You need the centos-java image or 
# build the centos-java image from the 
# docker directory
#########################################

FROM centos-java

MAINTAINER Tahsin Kabir

#Copying binary to the deployable containers fs
ADD  /target/universal/ /shobdo-app/bin

RUN unzip /shobdo-app/bin/shobdo-app-0.11-SNAPSHOT.zip -d /shobdo-app/bin/

RUN chmod +x /shobdo-app/bin/shobdo-app-0.11-SNAPSHOT/bin/shobdo-app

EXPOSE 9000

CMD /shobdo-app/bin/shobdo-app-0.11-SNAPSHOT/bin/shobdo-app -Dplay.crypto.secret=testtahsin
