package org.folio.locations.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.locations.domain.dto.ServicePointsUser;
import org.folio.locations.domain.dto.ServicePointsUsersCollection;
import org.folio.locations.rest.resource.ServicePointsUsersApi;
import org.folio.locations.service.crud.ServicePointUserService;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class ServicePointsUsersController implements ServicePointsUsersApi {

  private final ServicePointUserService service;

  @Override
  public ResponseEntity<ServicePointsUsersCollection> getServicePointsUsers(@Nullable String query, Integer limit,
                                                                            Integer offset) {
    return ResponseEntity.ok(service.getAll(query, limit, offset));
  }

  @Override
  public ResponseEntity<ServicePointsUser> getServicePointsUserById(UUID id) {
    return ResponseEntity.ok(service.getById(id));
  }

  @Override
  public ResponseEntity<ServicePointsUser> createServicePointsUser(ServicePointsUser servicePointsUser) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(servicePointsUser));
  }

  @Override
  public ResponseEntity<Void> updateServicePointsUserById(UUID id, ServicePointsUser servicePointsUser) {
    service.update(id, servicePointsUser);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> deleteServicePointsUserById(UUID id) {
    service.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> deleteServicePointsUsers() {
    service.deleteAll();
    return ResponseEntity.noContent().build();
  }
}
