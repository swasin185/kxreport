#!/bin/bash
apt install default-jdk-headless git maven mariadb-server tomcat10
mysql < ./script/sql/init-db.sql
