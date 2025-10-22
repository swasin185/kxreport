#!/bin/bash
apt install default-jdk-headless git maven mariadb-server tomcat10
git clone https://github.com/swasin185/kxreport
cd kxreport
mysql < ./sql/init-db.sql
mkdir /khgroup
mkdir /khgroup/report
# ./build.sh
