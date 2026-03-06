package org.folio.locations.repository;

import java.util.UUID;
import org.folio.locations.domain.entity.ServicePointUserEntity;
import org.folio.spring.cql.JpaCqlRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServicePointUserRepository extends JpaCqlRepository<ServicePointUserEntity, UUID> {
}
