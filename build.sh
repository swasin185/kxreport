export MAVEN_OPTS="-Djava.awt.headless=true"
mvn clean
mvn package
sudo rm -R /khgroup/report
sudo mkdir /khgroup/report
sudo cp -ur -R ./target/jasper/*.jasper /khgroup/report/
sudo rm -R /var/lib/tomcat10/lib/*.jar
sudo cp -ur ./target/kxreport/WEB-INF/lib/*.jar /var/lib/tomcat10/lib
sudo cp ./target/kxreport.war /var/lib/tomcat10/webapps
sudo systemctl restart tomcat10
