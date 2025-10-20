## Servie URL
```:url
http://host:8080/kxreport/
```
### GET list
list report files in HTML format
### GET json
list report files in JSON format
### GET getPDF
### POST execPDF
### POST openPDF
### POST filePDF
### POST fileCSV

## build command line
```:sh
mvn package
sudo cp -u ./target/kxreport/WEB-INF/lib/*.jar /var/lib/tomcat10/lib
sudo cp -u ./target/*.war /var/lib/tomcat10/webapps
sudo cp -ur ./target/jasper/* /khgroup/report
```

## Jasper Report Folder
```
khgroup/
├── report/
│   ├── app/
│   │   ├── database/
│   │   │   └── *.jasper
│   │   └── database/
│   │       └── *.jasper
│   ├── app/
│   │   ├── database/
│   │   │   └── *.jasper
│   │   └── database/
│   │       └── *.jasper
│   └── *.jasper
```

## MariaDB user
```:sh
sudo mysql
```
```:sql
drop user if exists 'kxreport'@'localhost';
create user 'kxreport'@'localhost' identified by 'kxreport';
grant select, execute, create temporary tables on *.* to 'kxreport'@'localhost';
create database kxtest;
```
## Deploy
copy -ur target/jasper /khgroup/report