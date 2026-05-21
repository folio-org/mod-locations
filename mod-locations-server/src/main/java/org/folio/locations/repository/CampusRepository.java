package org.folio.locations.repository;

import java.util.UUID;
import org.folio.locations.domain.entity.CampusEntity;
import org.folio.spring.cql.JpaCqlRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampusRepository extends JpaCqlRepository<CampusEntity, UUID> {
}
