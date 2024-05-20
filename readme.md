## build command line
```:sh
mvn package
sudo cp ./target/*.war /var/lib/tomcat10/webapps
sudo cp ./target/jasper/*.jasper /khgroup/report
```

## kxserv express && tomcat must run in https ssl mode
## create .keystore in home user dir
```:sh
keytool -genkey -alias tomcat -keyalg RSA
```

## modify /var/lib/tomcat9/conf/server.xml
```:xml
<Connector SSLEnabled="true" acceptCount="100" clientAuth="false"
    disableUploadTimeout="true" enableLookups="false" maxThreads="25"
    port="8443" keystoreFile="C:/Users/lokesh/.keystore" keystorePass="password"
    protocol="org.apache.coyote.http11.Http11NioProtocol" scheme="https"
    secure="true" sslProtocol="TLS" />
```

## Jasper Report Folder
```
[khgroup]
|-[report]
| |-[database]
| | |-- *.jasper
| |-- *.jasper
|-[report-app]
| |-[database]
| | |-- *.jasper
| |-- *.jasper

```