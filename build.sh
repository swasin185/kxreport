mvn package
sudo rm -R /var/lib/tomcat10/lib/*.jar
sudo cp -u ./target/kxreport/WEB-INF/lib/*.jar /var/lib/tomcat10/lib
sudo cp -u ./target/*.war /var/lib/tomcat10/webapps
sudo cp -ur ./target/jasper/* /khgroup/report
