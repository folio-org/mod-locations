package org.folio.locations.service.crud.impl;

import java.util.List;
import java.util.UUID;
import org.folio.locations.domain.dto.Campus;
import org.folio.locations.domain.dto.CampusesCollection;
import org.folio.locations.domain.entity.CampusEntity;
import org.folio.locations.domain.type.ResourceType;
import org.folio.locations.exception.CampusNotFoundException;
import org.folio.locations.mapper.CampusMapper;
import org.folio.locations.repository.CampusRepository;
import org.folio.locations.service.crud.AbstractCrudService;
import org.folio.locations.service.crud.CampusService;
import org.folio.locations.service.crud.GetAllContext;
import org.folio.locations.service.crud.ShadowFilterContext;
import org.folio.locations.service.event.DomainEventPublisher;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CampusServiceImpl
  extends AbstractCrudService<Campus, CampusesCollection, CampusEntity>
  implements CampusService {

  public CampusServiceImpl(CampusRepository repository, CampusMapper mapper,
                           FolioExecutionContext context, DomainEventPublisher publisher) {
    super(repository, mapper, c -> { }, context, publisher);
  }

  @Override
  public Class<Campus> getDtoClass() {
    return Campus.class;
  }

  protected String buildCqlFromContext(GetAllContext ctx) {
    var shadowCtx = ctx instanceof ShadowFilterContext s ? s : null;
    return buildCql(ctx.query(), shadowCtx != null ? shadowCtx.includeShadow() : null);
  }

  @Override
  protected CampusesCollection buildCollection(List<Campus> dtos, int totalRecords) {
    return new CampusesCollection(dtos, totalRecords);
  }

  @Override
  protected NotFoundException notFound(UUID id) {
    return new CampusNotFoundException(id);
  }

  @Override
  protected ResourceType resourceType() {
    return ResourceType.CAMPUS;
  }
}
