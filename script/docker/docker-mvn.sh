#!/bin/bash
docker volume create --name maven-repo
docker run --rm\
 -v maven-repo:/root/.m2\
 -v $PWD/../../:/usr/kxreport\
 maven:3.9-eclipse-temurin-25-alpine mvn -f /usr/kxreport/pom.xml clean package
