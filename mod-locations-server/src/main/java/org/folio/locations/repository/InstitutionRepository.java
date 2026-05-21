package org.folio.locations.repository;

import java.util.UUID;
import org.folio.locations.domain.entity.InstitutionEntity;
import org.folio.spring.cql.JpaCqlRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstitutionRepository extends JpaCqlRepository<InstitutionEntity, UUID> {
}
