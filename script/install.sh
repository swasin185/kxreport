#!/bin/bash
apt install default-jdk-headless git maven mariadb-server tomcat10
mysql < ./sql/init-db.sql
