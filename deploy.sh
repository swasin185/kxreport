#!/bin/bash
#mvn clean package
#cp -u ./target/kxreport/WEB-INF/lib/*.jar /var/lib/tomcat10/lib
#cp -u ./target/*.war /var/lib/tomcat10/webapps
#cp -uR ./target/jasper/* /khgroup/report
./docker-stop.sh
./docker-mvn.sh
./docker-mariadb.sh
./docker-tomcat.sh
