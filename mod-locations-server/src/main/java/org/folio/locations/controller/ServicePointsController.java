package org.folio.locations.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.locations.domain.dto.ServicePoint;
import org.folio.locations.domain.dto.ServicePointsCollection;
import org.folio.locations.rest.resource.ServicePointsApi;
import org.folio.locations.service.crud.ServicePointService;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class ServicePointsController implements ServicePointsApi {

  private final ServicePointService service;

  @Override
  public ResponseEntity<ServicePointsCollection> getServicePoints(@Nullable String query, Integer limit, Integer offset,
                                                                  Boolean includeRoutingServicePoints) {
    return ResponseEntity.ok(service.getServicePoints(query, limit, offset, includeRoutingServicePoints));
  }

  @Override
  public ResponseEntity<ServicePoint> getServicePointById(UUID id) {
    return ResponseEntity.ok(service.getById(id));
  }

  @Override
  public ResponseEntity<ServicePoint> createServicePoint(ServicePoint servicePoint) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(servicePoint));
  }

  @Override
  public ResponseEntity<Void> updateServicePointById(UUID id, ServicePoint servicePoint) {
    service.update(id, servicePoint);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> deleteServicePointById(UUID id) {
    service.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> deleteServicePoints() {
    service.deleteAll();
    return ResponseEntity.noContent().build();
  }
}

