#!/bin/bash
# docker pull maven:3.9.7-eclipse-temurin-22-alpine
# docker pull mariadb:10.5
# docker pull tomcat:10.1.10-jre17 
./docker-stop.sh
./docker-mvn.sh
./docker-mariadb.sh
./docker-tomcat.sh
