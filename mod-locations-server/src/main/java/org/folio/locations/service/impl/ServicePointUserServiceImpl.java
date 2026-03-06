package org.folio.locations.service.impl;

import java.util.List;
import java.util.UUID;
import org.folio.locations.domain.dto.ServicePointsUser;
import org.folio.locations.domain.dto.ServicePointsUsersCollection;
import org.folio.locations.domain.entity.ServicePointUserEntity;
import org.folio.locations.exception.ServicePointUserNotFoundException;
import org.folio.locations.mapper.ServicePointUserMapper;
import org.folio.locations.repository.ServicePointUserRepository;
import org.folio.locations.service.AbstractCrudService;
import org.folio.locations.service.ServicePointUserService;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.exception.NotFoundException;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicePointUserServiceImpl
  extends AbstractCrudService<ServicePointsUser, ServicePointsUsersCollection, ServicePointUserEntity>
  implements ServicePointUserService {

  public ServicePointUserServiceImpl(ServicePointUserRepository repository, ServicePointUserMapper mapper,
                                     FolioExecutionContext context) {
    super(repository, mapper, spu -> { }, context);
  }

  @Override
  @Transactional(readOnly = true)
  public ServicePointsUsersCollection getServicePointsUsers(@Nullable String query, Integer limit, Integer offset) {
    var cql = buildCql(query, true);
    return getCollection(cql, limit, offset);
  }

  @Override
  protected ServicePointsUsersCollection buildCollection(List<ServicePointsUser> dtos, int totalRecords) {
    return new ServicePointsUsersCollection(dtos, totalRecords);
  }

  @Override
  protected NotFoundException notFound(UUID id) {
    return new ServicePointUserNotFoundException(id);
  }
}
