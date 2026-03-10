package org.folio.locations.service.validator;

import org.apache.commons.lang3.StringUtils;
import org.folio.locations.domain.dto.ServicePoint;
import org.folio.locations.exception.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class ServicePointValidator implements DtoValidator<ServicePoint> {

  public static final String ERR_NAME_REQUIRED = "Service point name is required.";
  public static final String ERR_CODE_REQUIRED = "Service point code is required.";
  public static final String ERR_DISCOVERY_DISPLAY_NAME_REQUIRED = "Service point discoveryDisplayName is required.";
  public static final String ERR_PICKUP_WITHOUT_HOLD_EXPIRY =
    "Hold shelf expiry period must be specified when service point can be used for pickup.";
  public static final String ERR_HOLD_EXPIRY_WITHOUT_PICKUP =
    "Hold shelf expiry period cannot be specified when service point cannot be used for pickup";

  public void validate(ServicePoint servicePoint) {
    if (StringUtils.isBlank(servicePoint.getName())) {
      throw new ValidationException(ERR_NAME_REQUIRED);
    }
    if (StringUtils.isBlank(servicePoint.getCode())) {
      throw new ValidationException(ERR_CODE_REQUIRED);
    }
    if (StringUtils.isBlank(servicePoint.getDiscoveryDisplayName())) {
      throw new ValidationException(ERR_DISCOVERY_DISPLAY_NAME_REQUIRED);
    }

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
