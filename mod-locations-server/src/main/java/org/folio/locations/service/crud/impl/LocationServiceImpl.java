package org.folio.locations.service.crud.impl;

import java.util.List;
import java.util.UUID;
import org.folio.locations.domain.dto.Location;
import org.folio.locations.domain.dto.LocationsCollection;
import org.folio.locations.domain.entity.LocationEntity;
import org.folio.locations.domain.type.ResourceType;
import org.folio.locations.exception.LocationNotFoundException;
import org.folio.locations.mapper.LocationMapper;
import org.folio.locations.repository.LocationRepository;
import org.folio.locations.service.crud.AbstractCrudService;
import org.folio.locations.service.crud.GetAllContext;
import org.folio.locations.service.crud.LocationService;
import org.folio.locations.service.crud.ShadowFilterContext;
import org.folio.locations.service.event.DomainEventPublisher;
import org.folio.locations.service.validator.LocationValidator;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class LocationServiceImpl
  extends AbstractCrudService<Location, LocationsCollection, LocationEntity>
  implements LocationService {

  public LocationServiceImpl(LocationRepository repository, LocationMapper mapper,
                             FolioExecutionContext context, LocationValidator validator,
                             DomainEventPublisher publisher) {
    super(repository, mapper, validator, context, publisher);
  }

  @Override
  public Class<Location> getDtoClass() {
    return Location.class;
  }

  protected String buildCqlFromContext(GetAllContext ctx) {
    var shadowCtx = ctx instanceof ShadowFilterContext s ? s : null;
    return buildCql(ctx.query(), shadowCtx != null ? shadowCtx.includeShadow() : null);
  }

  @Override
  protected LocationsCollection buildCollection(List<Location> dtos, int totalRecords) {
    return new LocationsCollection(dtos, totalRecords);
  }

  @Override
  protected NotFoundException notFound(UUID id) {
    return new LocationNotFoundException(id);
  }

  @Override
  protected ResourceType resourceType() {
    return ResourceType.LOCATION;
  }
}
