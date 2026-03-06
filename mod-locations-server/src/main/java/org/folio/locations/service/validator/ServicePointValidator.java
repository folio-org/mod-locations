package org.folio.locations.service.validator;

import org.folio.locations.domain.dto.ServicePoint;
import org.folio.locations.exception.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class ServicePointValidator implements DtoValidator<ServicePoint> {

  public static final String ERR_PICKUP_WITHOUT_HOLD_EXPIRY =
    "Hold shelf expiry period must be specified when service point can be used for pickup.";
  public static final String ERR_HOLD_EXPIRY_WITHOUT_PICKUP =
    "Hold shelf expiry period cannot be specified when service point cannot be used for pickup";

  public void validate(ServicePoint servicePoint) {
    boolean isPickupLocation = Boolean.TRUE.equals(servicePoint.getPickupLocation());
    boolean hasHoldShelfExpiry = servicePoint.getHoldShelfExpiryPeriod() != null;

    if (isPickupLocation && !hasHoldShelfExpiry) {
      throw new ValidationException(ERR_PICKUP_WITHOUT_HOLD_EXPIRY);
    }
    if (!isPickupLocation && hasHoldShelfExpiry) {
      throw new ValidationException(ERR_HOLD_EXPIRY_WITHOUT_PICKUP);
    }
  }
}
