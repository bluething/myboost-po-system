# Dev BE Test - Habib Ali Machpud

# Prerequisites

Before running this application, ensure you have the following installed:

* Java 21
* Maven 3.8+
* PostgreSQL 17
* Podman/Docker
* Postman (optional) - For API testing

Verify your Java installation:  
`bashjava -version `  
Verify your Maven installation:  
`bashmvn -version`

## Database Setup

### Option 1: Podman/Docker compose (Recommended)

Go to root project and run from terminal 
`podman-compose -f podman-compose.yml up -d`  

### Option 2: Manually
1. Install PostgreSQL and start the service  
2. Create Database and User  
```sql
CREATE DATABASE habib_ali_machpud;
CREATE USER po_user WITH PASSWORD 'po_password';
GRANT ALL PRIVILEGES ON DATABASE your_full_name TO po_user;
```

### Init table and sample data (manually)
The tables and sample data automatically create and insert when application start.  
But, if you want to doing manually then go to root project and run from terminal
```
export DB_PASS='qz0I81Z!'
mvn liquibase:update
```
Assuming you choose Option 1. If you choose Option 2 then you need to adjust the DB_PASS value

## Running the Application

### Clone or Extract the Project

cd myboost-po-system

### Configure Database

Edit `src/main/resources/application.yml` with your database credentials.

### Build the Project

`mvn clean install`

### Run the Application

#### Option A: Using Maven
```bash
export DB_PASS=<<replace_with_your_passowrd_or_use_default_qz0I81Z!>>
mvn spring-boot:run
```

#### Using JAR file
#### Linux/Mac
```bash
export DB_PASS=<<replace_with_your_passowrd_or_use_default_qz0I81Z!>>
java -jar target/myboost-po-system-0.0.1-SNAPSHOT.jar
```

#### Windows (Command Prompt)
```bash
set DB_PASS=<<replace_with_your_passowrd_or_use_default_qz0I81Z!>>
java -jar target/myboost-po-system-0.0.1-SNAPSHOT.jar
```

#### Windows (PowerShell)
```bash
$env:DB_PASS=<<replace_with_your_passowrd_or_use_default_qz0I81Z!>>
java -jar target/myboost-po-system-0.0.1-SNAPSHOT.jar
```