package org.folio.locations.domain.type;

import lombok.Getter;

@Getter
public enum ResourceType {

  CAMPUS("campus"),
  INSTITUTION("institution"),
  LIBRARY("library"),
  LOCATION("location"),
  SERVICE_POINT("service-point"),
  SERVICE_POINT_USER("service-point-user");

  private final String resourceName;

  ResourceType(String resourceName) {
    this.resourceName = resourceName;
  }
}
