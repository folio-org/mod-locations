package org.folio.locations.service.crud.impl;

import java.util.UUID;
import org.folio.locations.domain.dto.Library;
import org.folio.locations.domain.entity.LibraryEntity;
import org.folio.locations.domain.type.ResourceType;
import org.folio.locations.exception.LibraryNotFoundException;
import org.folio.locations.mapper.LibraryMapper;
import org.folio.locations.repository.LibraryRepository;
import org.folio.locations.service.crud.AbstractCrudService;
import org.folio.locations.service.crud.GetAllContext;
import org.folio.locations.service.crud.LibraryService;
import org.folio.locations.service.crud.ShadowFilterContext;
import org.folio.locations.service.event.DomainEventPublisher;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class LibraryServiceImpl
  extends AbstractCrudService<Library, LibraryEntity>
  implements LibraryService {

  public LibraryServiceImpl(LibraryRepository repository, LibraryMapper mapper,
                            FolioExecutionContext context, DomainEventPublisher publisher) {
    super(repository, mapper, l -> {}, context, publisher);
  }

  @Override
  public Class<Library> getDtoClass() {
    return Library.class;
  }

  protected String buildCqlFromContext(GetAllContext ctx) {
    var shadowCtx = ctx instanceof ShadowFilterContext s ? s : null;
    return buildCql(ctx.query(), shadowCtx != null ? shadowCtx.includeShadow() : null);
  }

  @Override
  protected NotFoundException notFound(UUID id) {
    return new LibraryNotFoundException(id);
  }

  @Override
  protected ResourceType resourceType() {
    return ResourceType.LIBRARY;
  }
}
