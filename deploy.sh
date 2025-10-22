export MAVEN_OPTS="-Djava.awt.headless=true"
#mvn clean
mvn package
#sudo cp -ur ./target/jasper/*.jasper /khgroup/report/
sudo cp ./target/kxreport.war /var/lib/tomcat10/webapps
