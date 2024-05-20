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
## modify web.xml comment

Jasper Report Folder
/ - [khgroup]
    |-[report]
      |-[database]
        |- *.jasper
    |-[report-app]
      |-[database]
        |- *.jasper
