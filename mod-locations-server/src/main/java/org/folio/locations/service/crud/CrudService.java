package org.folio.locations.service.crud;

import java.util.UUID;

/**
 * Generic CRUD service interface for location-related domain objects.
 *
 * @param <D> DTO type
 * @param <C> DTO collection type
 */
public interface CrudService<D, C> {

  Class<D> getDtoClass();

  C getAll(GetAllContext context);

  D getById(UUID id);

  D create(D dto);

  void update(UUID id, D dto);

  void deleteById(UUID id);

  void deleteAll();
}
