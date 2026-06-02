package org.folio.locations.service.crud.impl;

import java.util.List;
import java.util.UUID;
import org.folio.locations.domain.dto.Institution;
import org.folio.locations.domain.dto.InstitutionsCollection;
import org.folio.locations.domain.entity.InstitutionEntity;
import org.folio.locations.domain.type.ResourceType;
import org.folio.locations.exception.InstitutionNotFoundException;
import org.folio.locations.mapper.InstitutionMapper;
import org.folio.locations.repository.InstitutionRepository;
import org.folio.locations.service.crud.AbstractCrudService;
import org.folio.locations.service.crud.GetAllContext;
import org.folio.locations.service.crud.InstitutionService;
import org.folio.locations.service.crud.ShadowFilterContext;
import org.folio.locations.service.event.DomainEventPublisher;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class InstitutionServiceImpl
  extends AbstractCrudService<Institution, InstitutionsCollection, InstitutionEntity>
  implements InstitutionService {

  public InstitutionServiceImpl(InstitutionRepository repository, InstitutionMapper mapper,
                                FolioExecutionContext context, DomainEventPublisher publisher) {
    super(repository, mapper, i -> { }, context, publisher);
  }

  @Override
  public Class<Institution> getDtoClass() {
    return Institution.class;
  }

  protected String buildCqlFromContext(GetAllContext ctx) {
    var shadowCtx = ctx instanceof ShadowFilterContext s ? s : null;
    return buildCql(ctx.query(), shadowCtx != null ? shadowCtx.includeShadow() : null);
  }

  @Override
  protected InstitutionsCollection buildCollection(List<Institution> dtos, int totalRecords) {
    return new InstitutionsCollection(dtos, totalRecords);
  }

  @Override
  protected NotFoundException notFound(UUID id) {
    return new InstitutionNotFoundException(id);
  }

  @Override
  protected ResourceType resourceType() {
    return ResourceType.INSTITUTION;
  }
}
