package org.folio.locations.support;

import static org.folio.locations.support.ApiResourceUrls.campusesResource;
import static org.folio.locations.support.ApiResourceUrls.institutionsResource;
import static org.folio.locations.support.ApiResourceUrls.librariesResource;
import static org.folio.locations.support.ApiResourceUrls.locationsResource;
import static org.folio.locations.support.BaseIT.doPost;

import java.util.List;
import java.util.UUID;
import lombok.experimental.UtilityClass;
import org.folio.locations.domain.dto.Campus;
import org.folio.locations.domain.dto.HoldShelfExpiryPeriod;
import org.folio.locations.domain.dto.Institution;
import org.folio.locations.domain.dto.Library;
import org.folio.locations.domain.dto.Location;
import org.folio.locations.domain.dto.ServicePoint;
import org.folio.locations.domain.dto.ServicePointsUser;
import org.jspecify.annotations.NonNull;

/**
 * Utility class for creating test data via API calls. Provides methods to create institutions,
 * campuses, libraries, locations, service points, and service points users. Each method generates a random UUID for
 * the created entity and returns it, allowing tests to easily reference the created data.
 * The class uses the {@link BaseIT#doPost} method to send POST
 * requests to the appropriate API endpoints defined in {@link ApiResourceUrls}.
 */
@UtilityClass
public class TestFactoryHelper {

  public static UUID createServicePoint(String name, String tenantId) {
    var id = UUID.randomUUID();
    var servicePoint = servicePoint(
      id,
      name,
      name.toLowerCase().replace(" ", "-"));
    doPost(ApiResourceUrls.servicePointsResource(), tenantId, servicePoint);
    return id;
  }

  public static UUID createCampus(UUID institutionId, String tenantId) {
    var id = UUID.randomUUID();
    var campus = campus(
      genString(id, "Camp-", 8),
      genString(id, "C-", 4),
      institutionId)
      .id(id);
    doPost(ApiResourceUrls.campusesResource(), tenantId, campus);
    return id;
  }

  public static UUID createCampus(String name, String code, UUID institutionId, String tenantId) {
    var id = UUID.randomUUID();
    var campus = campus(name, code, institutionId).id(id);
    doPost(campusesResource(), tenantId, campus);
    return id;
  }

  public static UUID createLibrary(UUID campusId, String tenantId) {
    var id = UUID.randomUUID();
    var library = library(
      genString(id, "Lib-", 8),
      genString(id, "L-", 4),
      campusId)
      .id(id);
    doPost(ApiResourceUrls.librariesResource(), tenantId, library);
    return id;
  }

  public static UUID createLibrary(String name, String code, UUID campusId, String tenantId) {
    var id = UUID.randomUUID();
    doPost(librariesResource(), tenantId, library(name, code, campusId).id(id));
    return id;
  }

  public static UUID createLocation(String name, String code, UUID institutionId, UUID campusId, UUID libraryId,
                                    UUID spId, String tenantId) {
    var id = UUID.randomUUID();
    var location = location(name, code, institutionId, campusId, libraryId, spId).id(id);
    doPost(locationsResource(), tenantId, location);
    return id;
  }

  public static UUID createInstitution(String tenantId) {
    var id = UUID.randomUUID();
    var institution = institution(genString(id, "Inst-", 8),
      genString(id, "I-", 4))
      .id(id);
    doPost(ApiResourceUrls.institutionsResource(), tenantId, institution);
    return id;
  }

  public static UUID createInstitution(String name, String code, String tenantId) {
    var id = UUID.randomUUID();
    var institution = institution(name, code).id(id);
    doPost(institutionsResource(), tenantId, institution);
    return id;
  }

  public static Location location(String name, String code, UUID institutionId, UUID campusId, UUID libraryId,
                                  UUID primaryServicePointId) {
    return new Location(name, code, institutionId, campusId, libraryId, primaryServicePointId).servicePointIds(
      List.of(primaryServicePointId));
  }

  public static Campus campus(String name, String code, UUID institutionId) {
    return new Campus(name, code, institutionId);
  }

  public static Institution institution(String name, String code) {
    return new Institution(name, code);
  }

  public static Library library(String name, String code, UUID campusId) {
    return new Library(name, code, campusId);
  }

  public static HoldShelfExpiryPeriod holdShelfExpiry(int duration, HoldShelfExpiryPeriod.IntervalIdEnum intervalId) {
    return new HoldShelfExpiryPeriod(duration, intervalId);
  }

  public static ServicePoint servicePoint(String name, String code) {
    return new ServicePoint(name, code, "Display: " + name);
  }

  public static ServicePoint servicePoint(UUID id, String name, String code) {
    return servicePoint(name, code).id(id);
  }

  public static ServicePointsUser servicePointsUser(UUID userId) {
    return new ServicePointsUser(userId);
  }

  public static ServicePointsUser servicePointsUser(UUID id, UUID userId) {
    return new ServicePointsUser(userId).id(id);
  }

  private static @NonNull String genString(UUID id, String prefix, int len) {
    return prefix + id.toString().substring(0, len);
  }
}
