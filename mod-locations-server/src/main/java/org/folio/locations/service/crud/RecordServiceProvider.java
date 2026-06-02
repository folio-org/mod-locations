package org.folio.locations.service.crud;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecordServiceProvider {

  private final List<CrudService<?, ?>> services;

  @SuppressWarnings("unchecked")
  public <D> CrudService<D, ?> getByDtoClass(Class<D> dtoClass) {
    return (CrudService<D, ?>) services.stream()
      .filter(s -> s.getDtoClass().equals(dtoClass))
      .findFirst()
      .orElseThrow(() -> new IllegalArgumentException("No CrudService found for DTO class: " + dtoClass.getName()));
  }
}
