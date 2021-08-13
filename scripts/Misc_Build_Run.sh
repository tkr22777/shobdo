#Play Activator
curl -O https://downloads.typesafe.com/typesafe-activator/1.3.10/typesafe-activator-1.3.10.zip
yum install unzip
unzip typesafe-activator-1.3.10.zip -d /
PATH=$PATH:/activator-dist-1.3.10/bin/

#Download java 8
wget --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u92-b14/jdk-8u92-linux-x64.rpm
rpm -ivh jdk-8u92-linux-x64.rpm

#Build (requires activator and java)
activator dist
cd target/universal
scp shobdo-app-1.0-SNAPSHOT.zip tahsin@www.shohay.org:~/path-to-dist

#Run (requires java)
unzip shobdo-app-1.0-SNAPSHOT.zip
cd shobdo-app-1.0-SNAPSHOT/bin
chmod +x shobdo-app
./shobdo-app -Dplay.crypto.secret=testtahsin
