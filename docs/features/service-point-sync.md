---
feature_id: service-point-sync
title: Service Point Sync
updated: 2026-05-20
---

# Service Point Sync

## What it does
Listens to service-point domain events from all tenant Kafka topics and automatically replicates create, update, and delete operations to all consortium member tenants. Only events originating from the central tenant are propagated; events from member tenants are silently ignored to prevent infinite re-propagation loops.

## Why it exists
In an ECS (Edge Computing Services / consortium) deployment a service point created on the central tenant must be available on every member tenant without operators manually duplicating the record. This feature keeps service points consistent across all consortium members automatically, so patron-facing and circulation behaviour is uniform throughout the consortium.

## Entry point(s)
| Type           | Topic                                                                                                                                              | Description                                                  |
|----------------|----------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------|
| Kafka Consumer | `#{folioKafkaProperties.listener['service-point'].topicPattern}` (resolved at runtime to `(${folio.environment}\.)(.*\.)locations\.service-point`) | Processes service-point domain events from all tenant topics |

### Event processing
- When processed: one message at a time (not batched); concurrency controlled by `folio.kafka.listener.service-point.concurrency`.
- Event types handled: `CREATE`, `UPDATE`, `DELETE`.
- Processing behavior: for each event from the central tenant, the corresponding create/update/delete operation is applied to every consortium member tenant by switching the Okapi tenant context per member. Per-member failures are logged as warnings and do not block propagation to remaining members.

## Business rules and constraints

### Feature activation
- The sync feature is entirely inactive when `folio.features.ecs-tlr.enabled` is `false` (the default). No Kafka listener is registered and no consortium-related beans are created. Enabling the feature requires a restart.

### Central-tenant gate
- Only events whose originating tenant is the consortium central tenant are propagated. Events from member tenants are silently discarded to prevent infinite re-propagation loops.
- Central-tenant status is determined by querying the User Tenants API. The result is cached per tenant for up to 3600 seconds (see Caching section).

### Propagation scope
- On a qualifying event, the corresponding operation (`CREATE`, `UPDATE`, or `DELETE`) is applied to every consortium member tenant.
- ECS request-routing service points (`ecsRequestRouting: true`) are replicated by the same mechanism as regular service points; there is no exclusion for routing-only points at the sync layer.

### Failure handling
- A failure to sync to an individual member tenant is logged as a warning and does not abort propagation to the remaining members (partial-success semantics).
- There is no automatic retry: if a member-tenant sync fails, the event is not reprocessed. Consistency must be recovered manually or by re-triggering the originating mutation.

### Limitations
- Newly added member tenants may not receive sync events for up to 3600 seconds after joining the consortium, due to cache TTL on the member-tenant list.
- If the central-tenant designation changes, the cached result may cause a former central tenant to continue propagating (or a new one to be skipped) for up to 3600 seconds.

## Caching
Two Caffeine caches back the consortium membership lookups and affect how quickly topology changes (new member tenants, central-tenant reassignment) are reflected:

| Cache                             | Key                                    | TTL    |
|-----------------------------------|----------------------------------------|--------|
| `consortium-central-tenant-cache` | executing tenant ID                    | 3600 s |
| `consortium-tenants-cache`        | executing tenant ID + source tenant ID | 3600 s |

Stale cache entries mean that newly added member tenants may not receive synced service-point changes until the next cache expiry.

## Configuration
| Variable                                   | Default | Purpose                                                           |
|--------------------------------------------|---------|-------------------------------------------------------------------|
| `ECS_TLR_FEATURE_ENABLED`                  | `false` | Enables or disables the sync feature entirely                     |
| `KAFKA_SERVICE_POINT_CONSUMER_CONCURRENCY` | `1`     | Number of concurrent Kafka listener threads                       |
| `ENV`                                      | `folio` | Environment prefix used when resolving the consumer topic pattern |

## Dependencies and interactions
- **Kafka** — consumes from topics matching `(${folio.environment}\.)(.*\.)locations\.service-point`. The consumed events are produced by [service-point-storage](service-point-storage.md).
- **User Tenants API** — called to determine whether the current tenant is a consortium central tenant and to resolve the central tenant ID.
- **Consortium Tenants API** — called to retrieve the list of member tenants for a given consortium ID.
