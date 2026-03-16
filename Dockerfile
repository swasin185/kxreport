# Stage 1: Build stage
FROM maven:3.9-eclipse-temurin-25-alpine AS build

WORKDIR /app

# Copy pom.xml and download dependencies (caching layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the rest of the source code
COPY . .

# Build the application and compile Jasper reports
# The jasperreports-maven-plugin will output .jasper files to target/jasper/
# Copy dependencies to target/lib for manual placement in Tomcat
RUN mvn clean package -DskipTests && \
    mvn dependency:copy-dependencies -DoutputDirectory=target/lib -DincludeScope=runtime

# Stage 2: Runtime stage
# Using Tomcat 11 with JRE 25 as specified in your docker.sh
FROM tomcat:11-jre25

# Create necessary directories
RUN mkdir -p /khgroup/report /etc/cert

# Copy libraries to Tomcat common lib to exclude them from the WAR
# Also include manual jars from webapp/WEB-INF/lib (like sarabun-fonts.jar)
COPY --from=build /app/target/lib/*.jar /usr/local/tomcat/lib/
COPY src/main/webapp/WEB-INF/lib/*.jar /usr/local/tomcat/lib/

# Set environment variables for Spring Boot (can be overridden at runtime)
# Overriding properties in application.properties:
# KXREPORT_REPORT_PATH -> kxreport.report.path
# KXREPORT_DB_URI -> kxreport.db.uri
# KXREPORT_DB_CONFIG -> kxreport.db.config
ENV KXREPORT_REPORT_PATH=/khgroup/report/ \
    KXREPORT_DB_URI=jdbc:mariadb://localhost:3306/ \
    KXREPORT_DB_CONFIG="?user=kxreport&password=kxreport&minPoolSize=1&maxPoolSize=99&maxIdleTime=60"

# Copy the compiled .jasper files from the build stage
COPY --from=build /app/target/jasper/ /khgroup/report/

# Copy the WAR file as kxreport.war to serve it at the context root /kxreport
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/kxreport.war /usr/local/tomcat/webapps/kxreport.war

# Copy SSL certificates and server.xml as used in your local scripts
COPY script/cert/cert.pem /etc/cert/cert.pem
COPY script/cert/key.pem /etc/cert/key.pem
COPY script/cert/server.xml /usr/local/tomcat/conf/server.xml

# Expose standard HTTP and HTTPS ports
EXPOSE 8080 8443

# Run Tomcat
CMD ["catalina.sh", "run"]
