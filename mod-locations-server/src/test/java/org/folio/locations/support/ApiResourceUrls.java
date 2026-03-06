package org.folio.locations.support;

import java.util.UUID;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ApiResourceUrls {

  private static final String SERVICE_POINTS = "/service-points";
  private static final String SERVICE_POINTS_USERS = "/service-points-users";
  private static final String INSTITUTIONS = "/location-units/institutions";
  private static final String CAMPUSES = "/location-units/campuses";
  private static final String LIBRARIES = "/location-units/libraries";

  public static String servicePointsResource() {
    return SERVICE_POINTS;
  }

  public static String servicePointResource(UUID id) {
    return SERVICE_POINTS + "/" + id;
  }

  public static String servicePointsUsersResource() {
    return SERVICE_POINTS_USERS;
  }

  public static String servicePointsUserResource(UUID id) {
    return SERVICE_POINTS_USERS + "/" + id;
  }

  public static String institutionsResource() {
    return INSTITUTIONS;
  }

  public static String institutionResource(UUID id) {
    return INSTITUTIONS + "/" + id;
  }

  public static String campusesResource() {
    return CAMPUSES;
  }

  public static String campusResource(UUID id) {
    return CAMPUSES + "/" + id;
  }

  public static String librariesResource() {
    return LIBRARIES;
  }

  public static String libraryResource(UUID id) {
    return LIBRARIES + "/" + id;
  }
}
