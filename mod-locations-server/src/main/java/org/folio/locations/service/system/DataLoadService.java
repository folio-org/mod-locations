package org.folio.locations.service.system;

import java.io.IOException;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.locations.domain.dto.Campus;
import org.folio.locations.domain.dto.Institution;
import org.folio.locations.domain.dto.Library;
import org.folio.locations.domain.dto.Location;
import org.folio.locations.domain.dto.ServicePoint;
import org.folio.locations.service.crud.ResourceServiceProvider;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Log4j2
@Service
@RequiredArgsConstructor
public class DataLoadService {

  private final JsonMapper jsonMapper;
  private final ResourcePatternResolver resourcePatternResolver;
  private final ResourceServiceProvider serviceProvider;

  public void loadReferenceData() {
    loadEntities("classpath:reference-data/service-points/*.json", ServicePoint.class,
      servicePoint -> serviceProvider.getByDtoClass(ServicePoint.class).create(servicePoint));
  }

  /**
   * Loads sample data in dependency order:
   * institutions → campuses → libraries → locations.
   */
  public void loadSampleData() {
    loadEntities("classpath:sample-data/institutions/*.json", Institution.class,
      institution -> serviceProvider.getByDtoClass(Institution.class).create(institution));
    loadEntities("classpath:sample-data/campuses/*.json", Campus.class,
      campus -> serviceProvider.getByDtoClass(Campus.class).create(campus));
    loadEntities("classpath:sample-data/libraries/*.json", Library.class,
      library -> serviceProvider.getByDtoClass(Library.class).create(library));
    loadEntities("classpath:sample-data/locations/*.json", Location.class,
      location -> serviceProvider.getByDtoClass(Location.class).create(location));
  }

  private <T> void loadEntities(String pattern, Class<T> type, Consumer<T> creator) {
    Resource[] resources;
    try {
      resources = resourcePatternResolver.getResources(pattern);
    } catch (IOException e) {
      log.error("Failed to scan resources at {}", pattern, e);
      return;
    }

    for (Resource resource : resources) {
      try {
        var entity = jsonMapper.readValue(resource.getInputStream(), type);
        creator.accept(entity);
        log.info("Loaded {} from {}", type.getSimpleName(), resource.getFilename());
      } catch (Exception e) {
        log.warn("Skipping {} from {}: {}", type.getSimpleName(), resource.getFilename(),
          e.getMessage());
      }
    }
  }
}
