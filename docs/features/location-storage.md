---
feature_id: location-storage
title: Location Storage
updated: 2026-05-20
---

# Location Storage

## What it does
Provides a CRUD REST API for managing the full four-level location hierarchy used in FOLIO: institutions, campuses, libraries, and physical locations. Callers can create, retrieve, update, and delete records at each level. Every successful mutation emits a domain event to Kafka so downstream consumers can react to changes.

## Why it exists
Circulation, inventory, and discovery modules all need a stable reference to where an item physically lives. This module is the system of record for that hierarchy, keeping location data in one place and broadcasting changes so dependent modules stay consistent without polling.

## Entry point(s)

### Locations
| Method | Path                             | Description                                           |
|--------|----------------------------------|-------------------------------------------------------|
| GET    | /location-storage/locations      | Returns a paginated, CQL-filterable list of locations |
| POST   | /location-storage/locations      | Creates a new location                                |
| GET    | /location-storage/locations/{id} | Returns a single location by ID                       |
| PUT    | /location-storage/locations/{id} | Replaces a location record                            |
| DELETE | /location-storage/locations/{id} | Deletes a location by ID                              |
| DELETE | /location-storage/locations      | Deletes all locations                                 |

### Institutions
| Method | Path                                | Description                                              |
|--------|-------------------------------------|----------------------------------------------------------|
| GET    | /location-storage/institutions      | Returns a paginated, CQL-filterable list of institutions |
| POST   | /location-storage/institutions      | Creates a new institution                                |
| GET    | /location-storage/institutions/{id} | Returns a single institution by ID                       |
| PUT    | /location-storage/institutions/{id} | Replaces an institution record                           |
| DELETE | /location-storage/institutions/{id} | Deletes an institution by ID                             |
| DELETE | /location-storage/institutions      | Deletes all institutions                                 |

### Campuses
| Method | Path                            | Description                                          |
|--------|---------------------------------|------------------------------------------------------|
| GET    | /location-storage/campuses      | Returns a paginated, CQL-filterable list of campuses |
| POST   | /location-storage/campuses      | Creates a new campus                                 |
| GET    | /location-storage/campuses/{id} | Returns a single campus by ID                        |
| PUT    | /location-storage/campuses/{id} | Replaces a campus record                             |
| DELETE | /location-storage/campuses/{id} | Deletes a campus by ID                               |
| DELETE | /location-storage/campuses      | Deletes all campuses                                 |

### Libraries
| Method | Path                             | Description                                           |
|--------|----------------------------------|-------------------------------------------------------|
| GET    | /location-storage/libraries      | Returns a paginated, CQL-filterable list of libraries |
| POST   | /location-storage/libraries      | Creates a new library                                 |
| GET    | /location-storage/libraries/{id} | Returns a single library by ID                        |
| PUT    | /location-storage/libraries/{id} | Replaces a library record                             |
| DELETE | /location-storage/libraries/{id} | Deletes a library by ID                               |
| DELETE | /location-storage/libraries      | Deletes all libraries                                 |

## Business rules and constraints

### Required fields

| Entity      | Required fields                                                                 |
|-------------|---------------------------------------------------------------------------------|
| Location    | `name`, `code`, `institutionId`, `campusId`, `libraryId`, `primaryServicePoint` |
| Institution | `name`, `code`                                                                  |
| Campus      | `name`, `code`, `institutionId`                                                 |
| Library     | `name`, `code`, `campusId`                                                      |

### Location — service point rules
- A location must have at least one service point in `servicePointIds`; an empty or absent list is rejected.
- The `primaryServicePoint` must be one of the IDs present in `servicePointIds`; mismatches are rejected.
- Each service point ID must appear at most once in `servicePointIds`; duplicates are rejected.

### Shadow locations
- A location may be flagged as a shadow location (`isShadow: true`). Shadow locations represent virtual or external-library placeholders; no real items are assignable to them.
- Collection listings exclude shadow locations by default. Callers must pass `includeShadow=true` to include them. This filter applies to all four levels of the hierarchy (institutions, campuses, libraries, and locations).

### Floating collections
- A location may be flagged as a floating collection (`isFloatingCollection: true`). Items belonging to such a location can be checked in or out at any other floating-collection location without triggering a transfer request.

### Audit metadata
- On create, `createdDate` and `createdByUserId` are set automatically from the Okapi execution context; the caller cannot supply these fields.
- On update, `updatedDate` and `updatedByUserId` are set automatically; the caller cannot supply these fields.

### Event publishing
- A domain event (`CREATE`, `UPDATE`, or `DELETE`) is published to Kafka for every successful mutation.
- Events are deferred until after the database transaction commits; a rolled-back transaction produces no event.

## Error behavior
- `400 Bad Request` — returned when the request body fails schema validation or business-rule validation (missing required fields, constraint violations).
- `404 Not Found` — returned when a requested location, institution, campus, or library ID does not exist.
- `500 Internal Server Error` — returned for unexpected server-side failures.

## Configuration
| Variable                                               | Default            | Purpose                                                |
|--------------------------------------------------------|--------------------|--------------------------------------------------------|
| `KAFKA_LOCATIONS_LOCATION_TOPIC_PARTITIONS`            | `1`                | Number of partitions for the `location` Kafka topic    |
| `KAFKA_LOCATIONS_LOCATION_TOPIC_REPLICATION_FACTOR`    | _(broker default)_ | Replication factor for the `location` Kafka topic      |
| `KAFKA_LOCATIONS_INSTITUTION_TOPIC_PARTITIONS`         | `1`                | Number of partitions for the `institution` Kafka topic |
| `KAFKA_LOCATIONS_INSTITUTION_TOPIC_REPLICATION_FACTOR` | _(broker default)_ | Replication factor for the `institution` Kafka topic   |
| `KAFKA_LOCATIONS_CAMPUS_TOPIC_PARTITIONS`              | `1`                | Number of partitions for the `campus` Kafka topic      |
| `KAFKA_LOCATIONS_CAMPUS_TOPIC_REPLICATION_FACTOR`      | _(broker default)_ | Replication factor for the `campus` Kafka topic        |
| `KAFKA_LOCATIONS_LIBRARY_TOPIC_PARTITIONS`             | `1`                | Number of partitions for the `library` Kafka topic     |
| `KAFKA_LOCATIONS_LIBRARY_TOPIC_REPLICATION_FACTOR`     | _(broker default)_ | Replication factor for the `library` Kafka topic       |
| `ENV`                                                  | `folio`            | Environment prefix used in Kafka topic names           |

## Dependencies and interactions
- **Kafka** — publishes domain events to tenant-scoped topics with the pattern `{env}.{tenantId}.locations.location`, `…locations.institution`, `…locations.campus`, `…locations.library` on every create, update, and delete. Events are sent after the database transaction commits to prevent consumers acting on rolled-back data.
