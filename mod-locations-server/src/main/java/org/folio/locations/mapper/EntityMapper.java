package org.folio.locations.mapper;

import org.mapstruct.MappingTarget;

public interface EntityMapper<D, E> {

  E toEntity(D dto);

  void updateEntity(D dto, @MappingTarget E entity);

  D toDto(E entity);
}
