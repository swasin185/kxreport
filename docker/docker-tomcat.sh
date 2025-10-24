#!/bin/bash
# systemctl stop tomcat9
docker run -d --rm \
  --name tomcat \
  --network "host" \
  -p 8888:8080 \
  -v $(pwd)/../target:/usr/local/tomcat/webapps \
  -v $(pwd)/../target/jasper:/khgroup/report \
  tomcat:11-jre25
#  -v $(pwd)/target/kxreport/WEB-INF/lib:/usr/local/tomcat/lib \
