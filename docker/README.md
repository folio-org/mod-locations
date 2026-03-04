# FOLIO mod-locations Docker Compose Setup

This Docker Compose configuration provides a complete local development environment for the FOLIO mod-locations module, including all necessary infrastructure components.

> **📍 Note:** All commands in this document are designed to be run from the `docker/` directory. If running from the terminal, make sure to `cd docker/` first.

## 📋 Table of Contents

  * [🎯 Overview](#-overview)
  * [🚀 Quick Start](#-quick-start)
  * [⚙️ Configuration](#-configuration)
  * [🔧 Services](#-services)
  * [📖 Usage](#-usage)
  * [🛠️ Development](#-development)

## 🎯 Overview

This Docker Compose setup includes:

- **mod-locations**: FOLIO module
- **PostgreSQL**: Database for module data persistence
- **pgAdmin**: Web-based database management tool
- **Apache Kafka**: Message broker for event-driven communication
- **Kafka UI**: Web interface for Kafka management

## 🚀 Quick Start

1. **Review and adjust environment variables in .env file** (optional)

2. **Build the module JAR** (required before building Docker image):
   ```shell
   mvn -f ../pom.xml clean package -DskipTests
   ```

3. **Build and start all services**:
   ```shell
   docker compose -f app-docker-compose.yml up -d
   ```

4. **Verify all services are running**:
   ```shell
   docker compose -f app-docker-compose.yml ps
   ```

5. **Check module logs**:
   ```shell
   docker compose -f app-docker-compose.yml logs -f mod-locations
   ```

## ⚙️ Configuration

### Environment Variables (.env)

| Variable                    | Default Value                     | Description                           |
|-----------------------------|-----------------------------------|---------------------------------------|
| `COMPOSE_PROJECT_NAME`      | `folio-mod-locations` | Docker Compose project name           |
| **Module Configuration**    |                                   |                                       |
| `ENV`                       | `folio`                           | Environment name                      |
| `MODULE_REPLICAS`           | `2`                               | Number of module instances to run     |
| **Database Configuration**  |                                   |                                       |
| `DB_HOST`                   | `postgres`                        | PostgreSQL hostname                   |
| `DB_PORT`                   | `5432`                            | PostgreSQL port                       |
| `DB_DATABASE`               | `folio`                   | Database name                         |
| `DB_USERNAME`               | `folio_admin`                     | Database username                     |
| `DB_PASSWORD`               | `folio_admin`                     | Database password                     |
| **pgAdmin Configuration**   |                                   |                                       |
| `PGADMIN_DEFAULT_EMAIL`     | `user@domain.com`                 | pgAdmin login email                   |
| `PGADMIN_DEFAULT_PASSWORD`  | `admin`                           | pgAdmin login password                |
| `PGADMIN_PORT`              | `5050`                            | pgAdmin web interface port            |
| **Kafka Configuration**     |                                   |                                       |
| `KAFKA_HOST`                | `kafka`                           | Kafka broker hostname                 |
| `KAFKA_PORT`                | `9093`                            | Kafka broker port (Docker internal)   |
| `KAFKA_TOPIC_PARTITIONS`    | `2`                               | Number of partitions for Kafka topics |
| `KAFKA_UI_PORT`             | `8090`                            | Kafka UI port                         |

## 🔧 Services

### mod-locations
- **Purpose**: FOLIO module
- **Access**: Dynamically assigned port (check with `docker compose ps`)
- **Scaling**: Configurable via `MODULE_REPLICAS`
- **Resource Limits**:
    - CPU: 0.5 cores (limit), 0.25 cores (reservation)
    - Memory: 512MB (limit), 256MB (reservation)

### PostgreSQL
- **Purpose**: Primary database for module data
- **Version**: PostgreSQL 16 Alpine
- **Access**: localhost:5432 (configurable via `DB_PORT`)
- **Credentials**: See `DB_USERNAME` and `DB_PASSWORD` in `.env`
- **Database**: See `DB_DATABASE` in `.env`

### pgAdmin
- **Purpose**: Database administration interface
- **Access**: http://localhost:5050 (configurable via `PGADMIN_PORT`)
- **Login**: Use `PGADMIN_DEFAULT_EMAIL` and `PGADMIN_DEFAULT_PASSWORD` from `.env`
- **Features**: Query editor, schema browser, data export/import

### Apache Kafka
- **Purpose**: Message broker for event-driven architecture
- **Mode**: KRaft (no Zookeeper required)
- **Listeners**:
    - Docker internal: `kafka:9093`
    - Host: `localhost:29092`

### Kafka UI
- **Purpose**: Web interface for Kafka management
- **Access**: http://localhost:8090 (configurable via `KAFKA_UI_PORT`)
- **Features**: Topic browsing, message viewing/producing, consumer group monitoring

## 📖 Usage

### Starting the Environment

```bash
# Start all services (infrastructure + module)
docker compose -f app-docker-compose.yml up -d
```

```bash
# Start only infrastructure services (for local development)
docker compose -f infra-docker-compose.yml up -d
```

```bash
# Start with build (if module code changed)
docker compose -f app-docker-compose.yml up -d --build
```

```bash
# Start specific service
docker compose -f app-docker-compose.yml up -d mod-locations
```

### Stopping the Environment

```bash
# Stop all services
docker compose -f app-docker-compose.yml down
```

```bash
# Stop infra services only
docker compose -f infra-docker-compose.yml down
```

```bash
# Stop and remove volumes (clean slate)
docker compose -f app-docker-compose.yml down -v
```

### Viewing Logs

```bash
# All services
docker compose -f app-docker-compose.yml logs
```

```bash
# Specific service
docker compose -f app-docker-compose.yml logs mod-locations
```

```bash
# Follow logs in real-time
docker compose -f app-docker-compose.yml logs -f mod-locations
```

```bash
# Last 100 lines
docker compose -f app-docker-compose.yml logs --tail=100 mod-locations
```

### Scaling the Module

```bash
# Scale to 3 instances
docker compose -f app-docker-compose.yml up -d --scale mod-locations=3
```

```bash
# Or modify MODULE_REPLICAS in .env and restart
echo "MODULE_REPLICAS=3" >> .env
docker compose -f app-docker-compose.yml up -d
```

### Cleanup and Reset

```bash
# Complete cleanup (stops containers, removes volumes)
docker compose -f app-docker-compose.yml down -v
```

```bash
# Recreate from scratch
docker compose -f app-docker-compose.yml down -v
mvn -f ../pom.xml clean package -DskipTests
docker compose -f app-docker-compose.yml up -d --build
```

## 🛠️ Development

### IntelliJ IDEA Usage

Run the main application class with the `dev` profile. Spring Boot will automatically use `infra-docker-compose.yml` for starting infrastructure services thanks to the `spring-boot-docker-compose` dependency.

**Steps:**
1. Ensure `spring.docker.compose.enabled=true` is set in `application-dev.yml`
2. Run/Debug the application from IntelliJ with the `dev` profile active
3. Spring Boot will automatically start the infrastructure containers
4. When you stop the application, the containers will remain running (configurable)

### Building the Module

It's expected that the module is packaged to a JAR before building the Docker image.

```shell
# Build the JAR
mvn -f ../pom.xml clean package
```

```shell
# Build the JAR without tests
mvn -f ../pom.xml clean package -DskipTests
```

```shell
# Build only the module Docker image
docker compose -f app-docker-compose.yml build mod-locations
```

```shell
# Build with no cache
docker compose -f app-docker-compose.yml build --no-cache mod-locations
```

### Connecting to Services

```bash
# Connect to PostgreSQL
docker compose -f app-docker-compose.yml exec postgres psql -U folio_admin -d folio
```

```bash
# Access Kafka container
docker compose -f app-docker-compose.yml exec kafka bash
```

```bash
# Connect to module container
docker compose -f app-docker-compose.yml exec mod-locations sh
```

```bash
# View database tables
docker compose -f app-docker-compose.yml exec postgres psql -U folio_admin -d folio -c "\dt"
```

### Troubleshooting

#### Module won't start
- Check if the JAR is built: `ls -lh ../mod-locations-server/target/*.jar`
- Check module logs: `docker compose -f app-docker-compose.yml logs mod-locations`
- Verify database is ready: `docker compose -f app-docker-compose.yml exec postgres pg_isready`

#### Database connection issues
- Verify PostgreSQL is running: `docker compose -f app-docker-compose.yml ps postgres`
- Check database credentials in `.env`
- Test connection: `docker compose -f app-docker-compose.yml exec postgres psql -U folio_admin -d folio -c "SELECT 1"`

#### Port conflicts
- Check if ports are already in use: `netstat -tulpn | grep -E '5432|8081|8090|9093|5050|29092'`
- Modify ports in `.env` file if needed

### Performance Tuning

#### Database Optimization
Adjust PostgreSQL settings by adding environment variables in `infra-docker-compose.yml`:
```yaml
environment:
  POSTGRES_SHARED_BUFFERS: 256MB
  POSTGRES_WORK_MEM: 10MB
  POSTGRES_MAX_CONNECTIONS: 100
```

#### Module Resource Limits
Adjust resource limits in `app-docker-compose.yml` under `deploy.resources`:
```yaml
resources:
  limits:
    cpus: "1.0"
    memory: "1G"
  reservations:
    cpus: "0.5"
    memory: "512M"
```

## 📚 Additional Resources

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Docker Compose Support](https://docs.spring.io/spring-boot/reference/features/dev-services.html#features.dev-services.docker-compose)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

