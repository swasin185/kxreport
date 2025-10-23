# 🧾 KXReport

## 📘 Introduction

**KXReport** is a Spring Boot application designed as a dedicated **Print Service** for the **KEEHIN Application**.  
It specializes in generating dynamic reports using **JasperReports7** and exposes this functionality via **RESTful API endpoints**.

The service handles:

-   Database connections
-   Dynamic report template selection
-   Processing input parameters (including creating temporary ID lists for complex queries)
-   Generating output reports in formats like **PDF** and **CSV**

---

## 🧩 Requirements

| Component            | Version |
| -------------------- | ------- |
| Java Runtime         |   17    |
| Tomcat               |   10    |
| MariaDB              |   10    |

---

## 🌐 Server Page

```url
http://host:8080/kxreport
```
See all parameters from index.html
See reports detail in report folder from list.html

## ⚙️ API Endpoints

| Method   | Path       | Produces           | Description                                                                                                                       |
| -------- | ---------- | ------------------ | --------------------------------------------------------------------------------------------------------------------------------- |
| **GET**  | `/json`    | `application/json` | Retrieves a list of all available compiled Jasper reports (`.jasper` files), including their name, last modified time, and size.  |
| **GET**  | `/getPDF`  | `application/pdf`  | Generates a report as a PDF stream using query parameters for GET requests.                                                       |
| **POST** | `/openPDF` | `application/pdf`  | Generates a report as a PDF stream for inline viewing (`Content-Disposition: inline`). Accepts parameters in a JSON request body. |
| **POST** | `/filePDF` | `text/plain`       | Generates a report as a PDF and saves it to a temporary file on the server. Returns the filename                                  |
| **POST** | `/fileCSV` | `text/plain`       | Generates a report as a CSV file and saves it to a temporary file on the server. Returns the filename.                            |

---

## 🛠️ Installation
install.sh
```bash
apt install default-jdk-headless git maven mariadb-server tomcat10
git clone https://github.com/swasin185/kxreport
cd kxreport
mysql < ./sql/init-db.sql
mkdir -p /khgroup/report
```

## Jasper FileFolder
/khgroup/report/
├── app/
│   ├── database/
│   │   └── \*.jasper
│   └── database/
│       └── \*.jasper
├── app/
│   ├── database/
│   │   └── \*.jasper
│   └── database/
│       └── \*.jasper
└── \*.jasper

## Developer
deploy.sh
```bash
export MAVEN_OPTS="-Djava.awt.headless=true"
mvn clean
mvn package
sudo rm -rf /khgroup/report
sudo mkdir -p /khgroup/report
sudo cp -ur ./target/jasper/*.jasper /khgroup/report/
sudo rm -rf /var/lib/tomcat10/lib/*.jar
sudo cp -ur ./target/kxreport/WEB-INF/lib/*.jar /var/lib/tomcat10/lib
sudo cp ./target/kxreport.war /var/lib/tomcat10/webapps
sudo systemctl restart tomcat10

```
