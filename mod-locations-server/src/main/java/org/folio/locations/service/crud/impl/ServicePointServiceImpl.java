package org.folio.locations.service.crud.impl;

import java.util.List;
import java.util.UUID;
import org.folio.locations.domain.dto.ServicePoint;
import org.folio.locations.domain.dto.ServicePointsCollection;
import org.folio.locations.domain.entity.ServicePointEntity;
import org.folio.locations.domain.entity.ServicePointStaffSlipId;
import org.folio.locations.domain.type.ResourceType;
import org.folio.locations.exception.ServicePointNotFoundException;
import org.folio.locations.mapper.ServicePointMapper;
import org.folio.locations.repository.ServicePointRepository;
import org.folio.locations.service.crud.AbstractCrudService;
import org.folio.locations.service.crud.ServicePointService;
import org.folio.locations.service.event.DomainEventPublisher;
import org.folio.locations.service.validator.ServicePointValidator;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.exception.NotFoundException;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicePointServiceImpl
  extends AbstractCrudService<ServicePoint, ServicePointsCollection, ServicePointEntity>
  implements ServicePointService {

  private static final String ECS_ROUTING_FILTER = " NOT ecsRequestRouting = true";

  public ServicePointServiceImpl(ServicePointRepository repository, ServicePointMapper mapper,
                                 FolioExecutionContext context, ServicePointValidator validator,
                                 DomainEventPublisher publisher) {
    super(repository, mapper, validator, context, publisher);
  }

  @Override
  @Transactional(readOnly = true)
  public ServicePointsCollection getServicePoints(@Nullable String query, Integer limit, Integer offset,
                                                  Boolean includeRoutingServicePoints) {
    var base = query != null ? "(" + query + ")" : ALL_RECORDS_CQL;
    var cql = Boolean.TRUE.equals(includeRoutingServicePoints) ? base : base + ECS_ROUTING_FILTER;
    return getCollection(cql, limit, offset);
  }

  @Override
  protected ServicePointsCollection buildCollection(List<ServicePoint> dtos, int totalRecords) {
    return new ServicePointsCollection(dtos, totalRecords);
  }

  @Override
  protected NotFoundException notFound(UUID id) {
    return new ServicePointNotFoundException(id);
  }

  @Override
  protected ResourceType resourceType() {
    return ResourceType.SERVICE_POINT;
  }

  @Override
  protected void beforeCreate(ServicePoint dto, ServicePointEntity entity) {
    syncStaffSlipIds(entity);
  }

  @Override
  protected void beforeUpdate(ServicePoint dto, ServicePointEntity entity) {
    if (dto.getHoldShelfExpiryPeriod() == null) {
      entity.setHoldShelfExpiryPeriodDuration(null);
      entity.setHoldShelfExpiryPeriodIntervalId(null);
    }
    syncStaffSlipIds(entity);
  }

  private void syncStaffSlipIds(ServicePointEntity entity) {
    entity.getStaffSlips().forEach(slip -> {
      if (slip.getId() == null) {
        slip.setId(new ServicePointStaffSlipId());
      }
      slip.getId().setServicePointId(entity.getId());
      slip.setServicePoint(entity);
    });
  }
}
