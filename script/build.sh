#!/bin/bash
# Define directory variables
TOMCAT="tomcat10"
REPORT_DIR="/khgroup/report"
TOMCAT_LIB_DIR="/var/lib/${TOMCAT}/lib"
TOMCAT_WEBAPPS_DIR="/var/lib/${TOMCAT}/webapps"
TARGET_DIR="./target" 
JASPER_SRC_DIR="${TARGET_DIR}/jasper"
KXREPORT_LIB_SRC_DIR="${TARGET_DIR}/kxreport/WEB-INF/lib"
WAR_SRC_PATH="${TARGET_DIR}/kxreport.war"

# --- Script Execution ---
export MAVEN_OPTS="-Djava.awt.headless=true"

echo "Running Maven clean and package..."
mvn clean package

echo "Setting up report directory: ${REPORT_DIR}"
sudo rm -r ${REPORT_DIR}/*.jasper
sudo rm -rf ${REPORT_DIR}/kxtest
sudo mkdir -p ${REPORT_DIR}
sudo cp -ur ${JASPER_SRC_DIR}/*/ ${REPORT_DIR}/
sudo find ${JASPER_SRC_DIR} -type f -name "*.jasper" -exec cp -ur -t /khgroup/report {} +


echo "Stop ${TOMCAT} service..."
sudo systemctl stop ${TOMCAT}

echo "Updating Tomcat libraries in: ${TOMCAT_LIB_DIR}"
sudo rm -rf ${TOMCAT_LIB_DIR}/*.jar
sudo cp -ur ${KXREPORT_LIB_SRC_DIR}/*.jar ${TOMCAT_LIB_DIR}

echo "Deploying WAR file to: ${TOMCAT_WEBAPPS_DIR}"
sudo cp ${WAR_SRC_PATH} ${TOMCAT_WEBAPPS_DIR}

sudo systemctl start ${TOMCAT}
echo "Start ${TOMCAT} service... [PRESS Q TO CLOSE]"
sudo systemctl status ${TOMCAT}
