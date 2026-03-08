# DocStore API

A production-grade Document Ingest REST API built with Java 17, Spring Boot 3, and AWS DocumentDB — deployed to AWS Elastic Beanstalk with a React frontend.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 (Amazon Corretto) |
| Framework | Spring Boot 3.2 |
| Database | AWS DocumentDB (MongoDB-compatible) |
| Hosting | AWS Elastic Beanstalk |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Frontend | React + Vite |
| SSL (API) | AWS Certificate Manager |
| SSL (UI) | Let's Encrypt / Certbot |

---

## Live URLs

| Service | URL |
|---|---|
| React Frontend | https://docs.gilliannewton.com |
| REST API | https://docsapi.gilliannewton.com |
| Swagger UI | https://docsapi.gilliannewton.com/swagger-ui.html |
| OpenAPI JSON | https://docsapi.gilliannewton.com/v3/api-docs |

---

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/docs` | Ingest a new document |
| `GET` | `/api/docs` | List all documents (supports `?title=` and `?content=` filters) |
| `GET` | `/api/docs/{id}` | Get a single document by MongoDB ObjectId |
| `GET` | `/api/docs/tag/{tag}` | Filter documents by tag |
| `DELETE` | `/api/docs/{id}` | Delete a document by ID |
| `GET` | `/api/docs/health` | Health check — used by EB load balancer |

---

## Project Structure

```
docstore/
├── pom.xml                                  ← Maven: Spring Web, MongoDB, SpringDoc, Validation
├── src/main/
│   ├── java/com/demo/docstore/
│   │   ├── DocStoreApplication.java          ← @SpringBootApplication entry point
│   │   ├── model/
│   │   │   └── IngestedDoc.java              ← @Document model → DocumentDB collection
│   │   ├── repository/
│   │   │   └── DocRepository.java            ← MongoRepository + derived queries
│   │   ├── controller/
│   │   │   └── DocController.java            ← REST endpoints + Swagger annotations
│   │   └── config/
│   │       ├── AppConfig.java                ← CORS + OpenAPI/Swagger config
│   │       └── GlobalExceptionHandler.java   ← Structured JSON error responses
│   └── resources/
│       └── application.properties            ← Port, MongoDB URI, Swagger paths
└── src/test/

docstore-ui/                                  ← React frontend (Vite)
├── src/
│   └── App.jsx                               ← Document list, ingest form, detail modal
└── .env                                      ← VITE_API_URL
```

---

## Local Development

### Prerequisites
- Java 17+
- Maven 3.9+
- MongoDB running locally or via Docker

```bash
# Start MongoDB with Docker
docker run -d -p 27017:27017 mongo:7
```

### Run the API

```bash
mvn spring-boot:run
```

API: http://localhost:5000/api/docs  
Swagger UI: http://localhost:5000/swagger-ui.html

### Run the React Frontend

```bash
cd docstore-ui
npm install
npm run dev
# http://localhost:5173
```

---

## AWS Architecture

```
Internet
    │
    ▼
AWS Elastic Beanstalk  (Spring Boot JAR, port 5000)
    │  (same VPC, private subnet)
    ▼
AWS DocumentDB         (MongoDB-compatible, TLS enforced, port 27017)
    
AWS Secrets Manager    (stores MONGODB_URI — never hardcoded)
AWS Certificate Manager (TLS cert for docsapi.gilliannewton.com)
```

DocumentDB is not publicly accessible — it only accepts connections from within the VPC from the Elastic Beanstalk security group.

---

## Environment Variables

Set these in the Elastic Beanstalk console under **Configuration → Environment properties**:

| Variable | Description |
|---|---|
| `MONGODB_URI` | Full DocumentDB connection string (from Secrets Manager) |
| `SERVER_PORT` | `5000` |

### DocumentDB URI format

```
mongodb://username:password@your-cluster.cluster-xxxx.us-east-1.docdb.amazonaws.com:27017/docstore
  ?tls=true
  &tlsCAFile=/var/app/current/global-bundle.pem
  &replicaSet=rs0
  &readPreference=secondaryPreferred
  &retryWrites=false
```

---

## Build & Deploy

### Build the JAR

```bash
mvn clean package -DskipTests
# Output: target/docstore-0.0.1-SNAPSHOT.jar
```

### Deploy to Elastic Beanstalk

1. AWS Console → Elastic Beanstalk → Upload `target/docstore-0.0.1-SNAPSHOT.jar`
2. Platform: Java / Corretto 17
3. Set environment variables (MONGODB_URI, SERVER_PORT)
4. Create environment

### Deploy React Frontend (DigitalOcean / Nginx)

```bash
# Build
cd docstore-ui && npm run build

# Copy to server
scp -r dist/* root@your-droplet-ip:/var/www/docs/

# SSL
sudo certbot --nginx -d docs.gilliannewton.com
```

---

## Custom Domains & SSL

| Subdomain | Target | SSL |
|---|---|---|
| `docsapi.gilliannewton.com` | CNAME → EB URL | AWS Certificate Manager |
| `docs.gilliannewton.com` | A record → DigitalOcean droplet | Let's Encrypt / Certbot |

See `DocStore_Complete_Guide.docx` Section 11 for full DNS and SSL setup instructions.

---

## Sample Request

```bash
# Ingest a document
curl -X POST https://docsapi.gilliannewton.com/api/docs \
  -H "Content-Type: application/json" \
  -d '{
    "title": "AWS Architecture Notes",
    "content": "DocumentDB is MongoDB-compatible and runs inside the VPC...",
    "tags": ["aws", "architecture"]
  }'

# Response
{
  "id": "65f1a2b3c4d5e6f7a8b9c0d1",
  "title": "AWS Architecture Notes",
  "content": "DocumentDB is MongoDB-compatible and runs inside the VPC...",
  "ingestedAt": "2026-03-05T14:30:00",
  "tags": ["aws", "architecture"]
}
```

---

## Built by

Gillian Newton · [gilliannewton.com](https://gilliannewton.com) · [@gillybops](https://github.com/gillybops)
