package org.folio.locations.repository;

import java.util.UUID;
import org.folio.locations.domain.entity.ServicePointUserEntity;
import org.folio.spring.cql.JpaCqlRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ServicePointUserRepository extends JpaCqlRepository<ServicePointUserEntity, UUID> {

  @Query("SELECT DISTINCT e FROM ServicePointUserEntity e WHERE :servicePointId MEMBER OF e.servicePointsIds")
  Page<ServicePointUserEntity> findByServicePointsIdsContaining(
    @Param("servicePointId") UUID servicePointId, Pageable pageable);
}
