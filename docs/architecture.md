# KxReport System Architecture

```mermaid
graph TB
    %% External Clients
    Client[Web Client/KEEHIN App]
    Browser[Web Browser]
    
    %% Load Balancer/Proxy
    LB[Nginx/SSL Termination<br/>Port: 8443]
    
    %% Application Server
    Tomcat[Apache Tomcat 10<br/>Port: 8080]
    
    %% Spring Boot Application
    KxReport[KxReport Spring Boot App<br/>WAR: kxreport.war]
    
    %% Spring Boot Components
    Controller[ReportController<br/>REST API Endpoints]
    DatabaseService[Database Service<br/>Connection Pooling]
    ReportEngine[JasperReports Engine 7<br/>Report Generation]
    
    %% API Endpoints
    API1[GET /json<br/>List Reports]
    API2[GET /getPDF<br/>PDF Generation]
    API3[POST /openPDF<br/>PDF Streaming]
    API4[POST /filePDF<br/>PDF File Export]
    API5[POST /fileCSV<br/>CSV Export]
    
    %% Data Layer
    ReportStore[Report Files Storage<br/>/khgroup/report/*.jasper]
    MariaDB[MariaDB Database<br/>Multiple Databases]
    
    %% File System Structure
    App1[app1/database1/*.jasper]
    App2[app2/database2/*.jasper]
    Default[*.jasper - Default]
    
    %% Configuration
    Config[application.properties<br/>Database & Report Config]
    
    %% Connections
    Client --> LB
    Browser --> LB
    LB --> Tomcat
    Tomcat --> KxReport
    KxReport --> Controller
    KxReport --> Config
    
    Controller --> API1
    Controller --> API2
    Controller --> API3
    Controller --> API4
    Controller --> API5
    
    API1 --> ReportStore
    API2 --> DatabaseService
    API3 --> DatabaseService
    API4 --> DatabaseService
    API5 --> DatabaseService
    
    DatabaseService --> MariaDB
    ReportEngine --> ReportStore
    
    ReportStore --> App1
    ReportStore --> App2
    ReportStore --> Default
    
    %% Styling
    classDef client fill:#e1f5fe,stroke:#01579b,stroke-width:2px
    classDef server fill:#f3e5f5,stroke:#4a148c,stroke-width:2px
    classDef app fill:#e8f5e8,stroke:#2e7d32,stroke-width:2px
    classDef data fill:#fff3e0,stroke:#e65100,stroke-width:2px
    classDef config fill:#fce4ec,stroke:#880e4f,stroke-width:2px
    
    class Client,Browser client
    class LB,Tomcat server
    class KxReport,Controller,DatabaseService,ReportEngine,API1,API2,API3,API4,API5 app
    class ReportStore,MariaDB,App1,App2,Default data
    class Config config
```

## Architecture Components

### **Client Layer**
- **Web Client/KEEHIN App**: Main consumer application
- **Web Browser**: Direct access for testing and demo
- **Protocol**: HTTPS (Port 8443) with SSL termination

### **Load Balancer/Proxy Layer**
- **Nginx**: Reverse proxy and SSL termination
- **SSL Certificate**: Generated via `./script/cert/genkey.sh`
- **Port**: 8443 (HTTPS) → 8080 (HTTP to Tomcat)

### **Application Server Layer**
- **Apache Tomcat 10**: Servlet container
- **Deployment**: WAR file (`kxreport.war`)
- **Port**: 8080 (internal)

### **Application Layer**
- **Spring Boot 3.5.6**: Main application framework
- **Java 17**: Runtime environment
- **Packaging**: WAR for Tomcat deployment

### **Service Layer**
- **ReportController**: REST API endpoints
- **Database Service**: Connection pooling with MariaDB
- **JasperReports Engine 7**: Report generation engine

### **API Endpoints**
| Method | Path | Output | Description |
|--------|------|--------|-------------|
| GET | `/json` | JSON | List all available Jasper reports |
| GET | `/getPDF` | PDF | Generate PDF with query parameters |
| POST | `/openPDF` | PDF Stream | Stream PDF with body parameters |
| POST | `/filePDF` | Text | Export PDF to file |
| POST | `/fileCSV` | Text | Export CSV to file |

### **Data Layer**
- **MariaDB**: Multi-database support
- **Connection Pooling**: Configurable pool sizes (0-99 connections)
- **Report Storage**: File system based (`/khgroup/report/`)

### **Report File Structure**
```
/khgroup/report/
├── app1/
│   ├── database1/
│   │   └── *.jasper
│   └── database2/
│       └── *.jasper
├── app2/
│   ├── database1/
│   └── database2/
└── *.jasper (default)
```

### **Configuration**
- **application.properties**: Database and report path configuration
- **Environment variables**: Runtime configuration
- **Logging**: SLF4J with file logging to `/var/lib/tomcat10/logs/kxreport.log`

## Data Flow

1. **Request Flow**: Client → Nginx → Tomcat → Spring Boot → Controller
2. **Report Generation**: Controller → JasperReports → Report Files → Output
3. **Database Access**: Controller → Database Service → MariaDB Connection Pool
4. **File Operations**: Report Engine → File System → PDF/CSV Output

## Security & Performance

- **SSL/TLS**: HTTPS encryption via Nginx
- **Connection Pooling**: Database connection optimization
- **Headless Mode**: `MAVEN_OPTS=-Djava.awt.headless=true` for server deployment
- **Logging**: Structured logging with timestamps and levels
