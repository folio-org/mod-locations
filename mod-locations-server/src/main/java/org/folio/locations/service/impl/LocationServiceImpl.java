package org.folio.locations.service.impl;

import java.util.List;
import java.util.UUID;
import org.folio.locations.domain.dto.Location;
import org.folio.locations.domain.dto.LocationsCollection;
import org.folio.locations.domain.entity.LocationEntity;
import org.folio.locations.exception.LocationNotFoundException;
import org.folio.locations.mapper.LocationMapper;
import org.folio.locations.repository.LocationRepository;
import org.folio.locations.service.AbstractCrudService;
import org.folio.locations.service.LocationService;
import org.folio.locations.service.validator.LocationValidator;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.exception.NotFoundException;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LocationServiceImpl
  extends AbstractCrudService<Location, LocationsCollection, LocationEntity>
  implements LocationService {

  public LocationServiceImpl(LocationRepository repository, LocationMapper mapper,
                             FolioExecutionContext context, LocationValidator validator) {
    super(repository, mapper, validator, context);
  }

  @Override
  @Transactional(readOnly = true)
  public LocationsCollection getAll(@Nullable String query, Integer limit, Integer offset, Boolean includeShadow) {
    return getCollection(buildCql(query, includeShadow), limit, offset);
  }

  @Override
  protected LocationsCollection buildCollection(List<Location> dtos, int totalRecords) {
    return new LocationsCollection(dtos, totalRecords);
  }

  @Override
  protected NotFoundException notFound(UUID id) {
    return new LocationNotFoundException(id);
  }
}
