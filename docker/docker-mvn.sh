#!/bin/bash
docker volume create --name maven-repo
docker run --rm\
 -v maven-repo:/root/.m2\
 -v $PWD/..:/usr/kxreport\
 maven:3.9.7-eclipse-temurin-22-alpine mvn -f /usr/kxreport/pom.xml clean package
