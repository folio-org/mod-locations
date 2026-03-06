package org.folio.locations.service.impl;

import java.util.List;
import java.util.UUID;
import org.folio.locations.domain.dto.Campus;
import org.folio.locations.domain.dto.CampusesCollection;
import org.folio.locations.domain.entity.CampusEntity;
import org.folio.locations.exception.CampusNotFoundException;
import org.folio.locations.mapper.CampusMapper;
import org.folio.locations.repository.CampusRepository;
import org.folio.locations.service.AbstractCrudService;
import org.folio.locations.service.CampusService;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.exception.NotFoundException;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CampusServiceImpl
  extends AbstractCrudService<Campus, CampusesCollection, CampusEntity>
  implements CampusService {

  public CampusServiceImpl(CampusRepository repository, CampusMapper mapper,
                           FolioExecutionContext context) {
    super(repository, mapper, context);
  }

  @Override
  @Transactional(readOnly = true)
  public CampusesCollection getAll(@Nullable String query, Integer limit, Integer offset, Boolean includeShadow) {
    var cql = buildCql(query, includeShadow);
    return getCollection(cql, limit, offset);
  }

  @Override
  protected CampusesCollection buildCollection(List<Campus> dtos, int totalRecords) {
    return new CampusesCollection(dtos, totalRecords);
  }

  @Override
  protected NotFoundException notFound(UUID id) {
    return new CampusNotFoundException(id);
  }
}
