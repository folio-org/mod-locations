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
import org.folio.locations.service.crud.InstitutionService;
import org.folio.locations.service.event.DomainEventPublisher;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.exception.NotFoundException;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InstitutionServiceImpl
  extends AbstractCrudService<Institution, InstitutionsCollection, InstitutionEntity>
  implements InstitutionService {

  public InstitutionServiceImpl(InstitutionRepository repository, InstitutionMapper mapper,
                                FolioExecutionContext context, DomainEventPublisher publisher) {
    super(repository, mapper, i -> { }, context, publisher);
  }

  @Override
  @Transactional(readOnly = true)
  public InstitutionsCollection getAll(@Nullable String query, Integer limit, Integer offset, Boolean includeShadow) {
    var cql = buildCql(query, includeShadow);
    return getCollection(cql, limit, offset);
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
