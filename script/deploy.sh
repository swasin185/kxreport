#!/bin/bash
export MAVEN_OPTS="-Djava.awt.headless=true"
mvn package -DskipTests
sudo cp ./target/kxreport.war /var/lib/tomcat10/webapps
