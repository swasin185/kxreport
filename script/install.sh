#!/bin/bash
apt install default-jdk-headless git maven mariadb-server tomcat10
sudo mysql < ./script/sql/init-db.sql
