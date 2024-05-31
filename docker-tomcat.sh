#!/bin/bash
docker run -d --rm \
  --name tomcat \
  --network bridge \
  -p 8888:8080 \
  -v $(pwd)/target:/usr/local/tomcat/webapps \
  -v $(pwd)/target/jasper:/khgroup/report \
  tomcat:10.1.10-jre17
#  -v $(pwd)/target/kxreport/WEB-INF/lib:/usr/local/tomcat/lib \
