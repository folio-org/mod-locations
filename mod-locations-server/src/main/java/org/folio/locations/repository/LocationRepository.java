package org.folio.locations.repository;

import java.util.UUID;
import org.folio.locations.domain.entity.LocationEntity;
import org.folio.spring.cql.JpaCqlRepository;

public interface LocationRepository extends JpaCqlRepository<LocationEntity, UUID> {
}
