package org.folio.locations.service.crud;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResourceServiceProvider {

  private final List<ResourceService<?>> services;

  @SuppressWarnings("unchecked")
  public <D> ResourceService<D> getByDtoClass(Class<D> dtoClass) {
    return (ResourceService<D>) services.stream()
      .filter(s -> s.getDtoClass().equals(dtoClass))
      .findFirst()
      .orElseThrow(() -> new IllegalArgumentException("No CrudService found for DTO class: " + dtoClass.getName()));
  }
}
