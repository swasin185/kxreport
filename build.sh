#!/bin/bash
# apt install default-jdk-headless git maven mariadb-server tomcat10
# git clone https://github.com/swasin185/kxreport
mysql < ./sql/init-db.sql
mkdir /khgroup
mkdir /khgroup/report
mvn  package
# cp -u ./target/kxreport/WEB-INF/lib/*.jar /var/lib/tomcat10/lib
cp -u ./target/*.war /var/lib/tomcat10/webapps
cp -uR ./target/jasper/* /khgroup/report