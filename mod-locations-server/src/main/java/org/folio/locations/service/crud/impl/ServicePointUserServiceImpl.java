package org.folio.locations.service.crud.impl;

import java.util.List;
import java.util.UUID;
import org.folio.locations.domain.dto.ServicePointsUser;
import org.folio.locations.domain.dto.ServicePointsUsersCollection;
import org.folio.locations.domain.entity.ServicePointUserEntity;
import org.folio.locations.domain.type.ResourceType;
import org.folio.locations.exception.ServicePointUserNotFoundException;
import org.folio.locations.mapper.ServicePointUserMapper;
import org.folio.locations.repository.ServicePointUserRepository;
import org.folio.locations.service.crud.AbstractCrudService;
import org.folio.locations.service.crud.GetAllContext;
import org.folio.locations.service.crud.ServicePointUserService;
import org.folio.locations.service.event.DomainEventPublisher;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ServicePointUserServiceImpl
  extends AbstractCrudService<ServicePointsUser, ServicePointsUsersCollection, ServicePointUserEntity>
  implements ServicePointUserService {

  public ServicePointUserServiceImpl(ServicePointUserRepository repository, ServicePointUserMapper mapper,
                                     FolioExecutionContext context, DomainEventPublisher publisher) {
    super(repository, mapper, spu -> { }, context, publisher);
  }

  @Override
  public Class<ServicePointsUser> getDtoClass() {
    return ServicePointsUser.class;
  }

  protected String buildCqlFromContext(GetAllContext ctx) {
    return buildCql(ctx.query(), true);
  }

  @Override
  protected ServicePointsUsersCollection buildCollection(List<ServicePointsUser> dtos, int totalRecords) {
    return new ServicePointsUsersCollection(dtos, totalRecords);
  }

  @Override
  protected NotFoundException notFound(UUID id) {
    return new ServicePointUserNotFoundException(id);
  }

  @Override
  protected ResourceType resourceType() {
    return ResourceType.SERVICE_POINT_USER;
  }
}
