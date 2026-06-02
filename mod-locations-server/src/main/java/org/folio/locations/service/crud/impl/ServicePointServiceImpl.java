package org.folio.locations.service.crud.impl;

import java.util.UUID;
import org.folio.locations.domain.dto.ServicePoint;
import org.folio.locations.domain.entity.ServicePointEntity;
import org.folio.locations.domain.entity.ServicePointStaffSlipId;
import org.folio.locations.domain.type.ResourceType;
import org.folio.locations.exception.ServicePointNotFoundException;
import org.folio.locations.mapper.ServicePointMapper;
import org.folio.locations.repository.ServicePointRepository;
import org.folio.locations.service.crud.AbstractCrudService;
import org.folio.locations.service.crud.GetAllContext;
import org.folio.locations.service.crud.ServicePointFilterContext;
import org.folio.locations.service.crud.ServicePointService;
import org.folio.locations.service.event.DomainEventPublisher;
import org.folio.locations.service.validator.ServicePointValidator;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ServicePointServiceImpl
  extends AbstractCrudService<ServicePoint, ServicePointEntity>
  implements ServicePointService {

  private static final String ECS_ROUTING_FILTER = " NOT ecsRequestRouting = true";

  public ServicePointServiceImpl(ServicePointRepository repository, ServicePointMapper mapper,
                                 FolioExecutionContext context, ServicePointValidator validator,
                                 DomainEventPublisher publisher) {
    super(repository, mapper, validator, context, publisher);
  }

  @Override
  public Class<ServicePoint> getDtoClass() {
    return ServicePoint.class;
  }

  protected String buildCqlFromContext(GetAllContext ctx) {
    var spCtx = ctx instanceof ServicePointFilterContext s ? s : null;
    var base = ctx.query() != null ? "(" + ctx.query() + ")" : ALL_RECORDS_CQL;
    var includeRouting = spCtx != null && Boolean.TRUE.equals(spCtx.includeRoutingServicePoints());
    return includeRouting ? base : base + ECS_ROUTING_FILTER;
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
    if (dto.getStaffSlips() == null) {
      entity.getStaffSlips().clear();
    }
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
