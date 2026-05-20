# Module Features

This module provides the following features:

| Feature | Description |
|---------|-------------|
| [Location Storage](features/location-storage.md) | CRUD API for the four-level location hierarchy (institutions, campuses, libraries, locations) with Kafka domain event publishing on every mutation |
| [Service Point Storage](features/service-point-storage.md) | CRUD API for service points with validation rules and Kafka domain event publishing on every mutation |
| [Service Point Sync](features/service-point-sync.md) | ECS consortium sync that replicates service-point changes from the central tenant to all member tenants via Kafka |
