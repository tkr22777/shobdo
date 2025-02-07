#######################################
# Dockerfile to run the dictionary app
#######################################

FROM openjdk:8-jre-alpine

#Copying binary to the deployable containers fs
ADD  /target/universal/ /shobdo-app/bin

RUN apk add unzip && \
    apk add bash && \
    unzip /shobdo-app/bin/shobdo-app-0.11-SNAPSHOT.zip -d /shobdo-app/bin/

RUN chmod +x /shobdo-app/bin/shobdo-app-0.11-SNAPSHOT/bin/shobdo-app

EXPOSE 9000

CMD /shobdo-app/bin/shobdo-app-0.11-SNAPSHOT/bin/shobdo-app -Dplay.crypto.secret=testtahsin
