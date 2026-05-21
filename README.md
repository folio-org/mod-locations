# mod-locations

[![FOLIO](https://img.shields.io/badge/FOLIO-Module-blue)](https://www.folio.org/)
[![Release Version](https://img.shields.io/github/v/release/folio-org/mod-locations?sort=semver&label=Latest%20Release)](https://github.com/folio-org/mod-locations/releases)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=org.folio%3Amod-locations&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=org.folio%3Amod-locations)
[![License](https://img.shields.io/github/license/folio-org/mod-locations)](LICENSE)

Copyright © 2026 The Open Library Foundation

This software is distributed under the terms of the Apache License,Version 2.0. See the file "[LICENSE](LICENSE)" for more information.

## Introduction

mod-locations is a FOLIO module for managing locations and service points.

## Development

### Prerequisites
- Java 21
- Maven 3.6+
- Docker and Docker Compose (for local development)
- [pre-commit](https://pre-commit.com/)

### Building the Module
```bash
mvn clean install
```

### Running Tests
```bash
# Unit tests
mvn test

# Integration tests
mvn verify
```

### Running Locally

**Option 1: Using Spring Boot with dev profile (recommended for development)**
```bash
# Automatically starts PostgreSQL, Kafka, and other infrastructure via Docker Compose
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Option 2: Manual infrastructure setup**
```bash
# Start infrastructure services
docker compose -f docker/infra-docker-compose.yml up -d

# Run the module
mvn spring-boot:run
```

**Option 3: Full Docker setup**
```bash
# Build and run everything in Docker
docker compose -f docker/app-docker-compose.yml up -d

# Check logs
docker compose -f docker/app-docker-compose.yml logs -f mod-locations
```

See [docker/README.md](docker/README.md) for detailed Docker Compose documentation.

### Code Style
The project uses Checkstyle to enforce code quality. Run:
```bash
mvn checkstyle:check
```

### Git Hooks

The repository uses [pre-commit](https://pre-commit.com/) to run API linting before every push. Each developer needs to activate it once:

```bash
pipx install pre-commit
pre-commit install --hook-type pre-push --hook-type commit-msg
```

After that:
- `scripts/lint-api.sh` runs on every `git push` and blocks if Spectral finds issues
- `mvn checkstyle:check` runs on every `git push` and blocks if there are code style violations
- `gitlint` runs on every `git commit` and blocks if the commit message doesn't include a `Closes: MODLOC-<number>` footer line, or if a conventional commit scope is used that doesn't match a feature in `docs/features/`

Example valid commit message:
```
fix(location-storage): correct campus 404 response

Closes: MODLOC-123
```

Valid scopes are derived from filenames in `docs/features/` (e.g. `location-storage`, `service-point-storage`, `service-point-sync`). Adding a new feature doc automatically makes its name a valid scope. The hook definition is committed to the repo (`.pre-commit-config.yaml`) and shared with the whole team — only the one-time install step is per-machine.

> **Note:** To make API linting a hard requirement on pull requests, enable branch protection on `master` and set the `Spectral Lint` GitHub Actions check as a required status check.

## Additional Information

### Issue tracker

See project [MODLOC](https://issues.folio.org/browse/MODLOC) and
the [Guidelines for FOLIO issue tracker](https://dev.folio.org/guidelines/issue-tracker/).

### API Documentation

This module's [API documentation](https://dev.folio.org/reference/api/#mod-locations).

### Code analysis

[SonarQube analysis](https://sonarcloud.io/dashboard?id=org.folio%3Amod-locations).