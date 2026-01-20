#!/bin/bash
# systemctl stop tomcat9
docker run -d --rm \
  --name tomcat \
  --network "host" \
  -v $(pwd)/../../target:/usr/local/tomcat/webapps \
  -v $(pwd)/../../target/jasper:/khgroup/report \
  -v $(pwd)/../cert/cert.pem:/etc/ssl/certs/cert.pem:ro \
  -v $(pwd)/../cert/key.pem:/etc/ssl/private/key.pem:ro \
  -v $(pwd)/../cert/server.xml:/usr/local/tomcat/conf/server.xml:ro \
  tomcat:11-jre25
