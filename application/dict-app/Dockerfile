########################################
# Dockerfile to build the dictionary app
# You need the centos-java image or 
# build the centos-java image from the 
# docker directory
#########################################

FROM stupefied/centos-java8

MAINTAINER Tahsin Kabir

#Copying binary to the deployable containers fs
ADD  /target/universal/ /dict-app/bin

RUN unzip /dict-app/bin/dict-app-0.11-SNAPSHOT.zip -d /dict-app/bin/

RUN chmod +x /dict-app/bin/dict-app-0.11-SNAPSHOT/bin/dict-app

EXPOSE 9000

CMD /dict-app/bin/dict-app-0.11-SNAPSHOT/bin/dict-app -Dplay.crypto.secret=testtahsin
