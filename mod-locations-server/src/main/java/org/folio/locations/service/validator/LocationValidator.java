package org.folio.locations.service.validator;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.folio.locations.domain.dto.Location;
import org.folio.locations.exception.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class LocationValidator implements DtoValidator<Location> {

  public static final String ERR_NO_SERVICE_POINTS =
    "A location must have at least one Service Point assigned.";
  public static final String ERR_PRIMARY_NOT_IN_SERVICE_POINTS =
    "A Location's Primary Service point must be included as a Service Point.";
  public static final String ERR_DUPLICATE_SERVICE_POINTS =
    "A Service Point can only appear once on a Location.";

  public void validate(Location location) {
    List<UUID> servicePointIds = location.getServicePointIds();

    if (servicePointIds == null || servicePointIds.isEmpty()) {
      throw new ValidationException(ERR_NO_SERVICE_POINTS);
    }
    if (!servicePointIds.contains(location.getPrimaryServicePoint())) {
      throw new ValidationException(ERR_PRIMARY_NOT_IN_SERVICE_POINTS);
    }
    if (servicePointIds.size() != new HashSet<>(servicePointIds).size()) {
      throw new ValidationException(ERR_DUPLICATE_SERVICE_POINTS);
    }
  }
}
