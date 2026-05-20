---
feature_id: service-point-storage
title: Service Point Storage
updated: 2026-05-20
---

# Service Point Storage

## What it does
Provides CRUD REST APIs for managing service points — the physical desks or counters where library staff interact with patrons — and for managing the assignment of service points to users. Callers can create, retrieve, update, and delete both service point records and service-point-user assignments. Every successful mutation on either resource emits a domain event to Kafka.

## Why it exists
Service points are a core reference entity in FOLIO: locations reference them (as primary and associated service points), and circulation rules use them to determine where items can be checked out, returned, and held. Service-point-user assignments tell the system which desks a specific user is authorised to operate at and which one is their default. Centralising both resources in one module with a published event stream allows other modules to stay consistent without polling.

## Entry point(s)

### Service points
| Method | Path                                       | Description                                                |
|--------|--------------------------------------------|------------------------------------------------------------|
| GET    | /service-point-storage/service-points      | Returns a paginated, CQL-filterable list of service points |
| POST   | /service-point-storage/service-points      | Creates a new service point                                |
| GET    | /service-point-storage/service-points/{id} | Returns a single service point by ID                       |
| PUT    | /service-point-storage/service-points/{id} | Replaces a service point record                            |
| DELETE | /service-point-storage/service-points/{id} | Deletes a service point by ID                              |
| DELETE | /service-point-storage/service-points      | Deletes all service points                                 |

### Service-point-user assignments
| Method | Path                                             | Description                                                                |
|--------|--------------------------------------------------|----------------------------------------------------------------------------|
| GET    | /service-point-storage/service-points-users      | Returns a paginated, CQL-filterable list of service-point-user assignments |
| POST   | /service-point-storage/service-points-users      | Creates a new service-point-user assignment                                |
| GET    | /service-point-storage/service-points-users/{id} | Returns a single assignment by ID                                          |
| PUT    | /service-point-storage/service-points-users/{id} | Replaces an assignment record                                              |
| DELETE | /service-point-storage/service-points-users/{id} | Deletes an assignment by ID                                                |
| DELETE | /service-point-storage/service-points-users      | Deletes all assignments                                                    |

## Business rules and constraints

### Service points — required fields
- `name`, `code`, and `discoveryDisplayName` are required on every create and update. Blank or whitespace-only values are rejected.

### Service points — pickup location and hold shelf
- If `pickupLocation` is `true`, `holdShelfExpiryPeriod` **must** be provided; omitting it is rejected.
- If `pickupLocation` is `false` or absent, `holdShelfExpiryPeriod` **must not** be provided; including it is rejected.
- `holdShelfClosedLibraryDateManagement` defaults to `Keep_the_current_due_date` when not specified.

### Service points — ECS request-routing
- A service point may be flagged as an ECS request-routing point (`ecsRequestRouting: true`). These points are internal routing artefacts and are excluded from GET collection results by default.
- Callers must pass `includeRoutingServicePoints=true` to include ECS routing points in collection results.

### Service points — update semantics
- `staffSlips` is fully replaced on update. Omitting the field from the request body clears all existing staff slip associations.
- Omitting `holdShelfExpiryPeriod` on an update clears both the duration and interval stored values.

### Service-point-user assignments — required fields
- `userId` is the only required field. `servicePointsIds` and `defaultServicePointId` are optional.

### Service-point-user assignments — constraints
- There is no server-side enforcement that `defaultServicePointId` appears in `servicePointsIds`; callers are responsible for consistency between these two fields.
- There is no server-side enforcement that the referenced service points or the user exist; referential integrity is the caller's responsibility.

### Audit metadata
- On create, `createdDate` and `createdByUserId` are set automatically from the Okapi execution context.
- On update, `updatedDate` and `updatedByUserId` are set automatically from the Okapi execution context.
- This applies to both service points and service-point-user assignments.

### Event publishing
- A domain event (`CREATE`, `UPDATE`, or `DELETE`) is published to Kafka for every successful mutation on both resources.
- Events are deferred until after the database transaction commits; a rolled-back transaction produces no event.

## Error behavior
- `400 Bad Request` — returned when the request body fails schema validation or business-rule validation (blank required fields, hold-shelf/pickup constraint violation).
- `404 Not Found` — returned when a requested service point or assignment ID does not exist (single-record GET, PUT, DELETE by ID).

## Configuration
| Variable                                                      | Default            | Purpose                                                       |
|---------------------------------------------------------------|--------------------|---------------------------------------------------------------|
| `KAFKA_LOCATIONS_SERVICE_POINT_TOPIC_PARTITIONS`              | `1`                | Number of partitions for the `service-point` Kafka topic      |
| `KAFKA_LOCATIONS_SERVICE_POINT_TOPIC_REPLICATION_FACTOR`      | _(broker default)_ | Replication factor for the `service-point` Kafka topic        |
| `KAFKA_LOCATIONS_SERVICE_POINT_USER_TOPIC_PARTITIONS`         | `1`                | Number of partitions for the `service-point-user` Kafka topic |
| `KAFKA_LOCATIONS_SERVICE_POINT_USER_TOPIC_REPLICATION_FACTOR` | _(broker default)_ | Replication factor for the `service-point-user` Kafka topic   |
| `ENV`                                                         | `folio`            | Environment prefix used in Kafka topic names                  |

## Dependencies and interactions
- **Kafka** — publishes domain events to tenant-scoped topics `{env}.{tenantId}.locations.service-point` and `{env}.{tenantId}.locations.service-point-user` on every create, update, and delete. Events are sent after the database transaction commits to prevent consumers acting on rolled-back data.
