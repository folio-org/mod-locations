package org.folio.locations.service.crud;

import java.util.UUID;
import org.folio.locations.domain.dto.ServicePointsUser;
import org.folio.locations.domain.dto.ServicePointsUsersCollection;
import org.jspecify.annotations.Nullable;

public interface ServicePointUserService {

  ServicePointsUsersCollection getServicePointsUsers(@Nullable String query, Integer limit, Integer offset);

  ServicePointsUser getById(UUID id);

  ServicePointsUser create(ServicePointsUser servicePointsUser);

  void update(UUID id, ServicePointsUser servicePointsUser);

  void deleteById(UUID id);

  void deleteAll();
}
