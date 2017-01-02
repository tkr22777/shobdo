###########################################
# Dockerfile to build the dictionary app
# Based on Centos
###########################################

FROM base-app

MAINTAINER Tahsin Kabir

#The copy the binary from the local target dir! Do not mount because we dont want to mount in production
ADD ./target/universal /dict-app/bin

RUN unzip /dict-app/bin/dict-app-0.11-SNAPSHOT.zip -d /dict-app/bin/

RUN chmod +x /dict-app/bin/dict-app-0.11-SNAPSHOT/bin/dict-app

EXPOSE 9000

CMD /dict-app/bin/dict-app-0.11-SNAPSHOT/bin/dict-app -Dplay.crypto.secret=testtahsin

#ENTRYPOINT usr/bin/mongod
