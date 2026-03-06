package org.folio.locations.support;

import java.util.UUID;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ApiUrls {

  private static final String SERVICE_POINTS = "/service-points";
  private static final String SERVICE_POINTS_USERS = "/service-points-users";

  public static String servicePoints() {
    return SERVICE_POINTS;
  }

  public static String servicePoint(UUID id) {
    return SERVICE_POINTS + "/" + id;
  }

  public static String servicePointsUsers() {
    return SERVICE_POINTS_USERS;
  }

  public static String servicePointsUser(UUID id) {
    return SERVICE_POINTS_USERS + "/" + id;
  }
}
