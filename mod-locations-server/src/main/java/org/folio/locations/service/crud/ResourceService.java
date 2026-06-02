package org.folio.locations.service.crud;

import java.util.UUID;

/**
 * Generic CRUD service interface for domain objects.
 *
 * @param <D> DTO type
 */
public interface ResourceService<D> {

  Class<D> getDtoClass();

  ResourceCollection<D> getAll(GetAllContext context);

  D getById(UUID id);

  D create(D dto);

  void update(UUID id, D dto);

  void deleteById(UUID id);

  void deleteAll();
}
