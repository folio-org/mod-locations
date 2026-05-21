package org.folio.locations.service.crud;

import java.util.UUID;
import org.folio.locations.domain.dto.ServicePoint;
import org.folio.locations.domain.dto.ServicePointsCollection;
import org.jspecify.annotations.Nullable;

public interface ServicePointService {

  ServicePointsCollection getServicePoints(@Nullable String query, Integer limit, Integer offset,
                                           Boolean includeRoutingServicePoints);

  ServicePoint getById(UUID id);

  ServicePoint create(ServicePoint servicePoint);

  void update(UUID id, ServicePoint servicePoint);

  void deleteById(UUID id);

  void deleteAll();
}
