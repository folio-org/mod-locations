package org.folio.locations.service.validator;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;
import org.folio.locations.domain.dto.HoldShelfExpiryPeriod;
import org.folio.locations.domain.dto.ServicePoint;
import org.folio.locations.exception.ValidationException;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@UnitTest
class ServicePointValidatorTest {

  private final ServicePointValidator validator = new ServicePointValidator();

  @Test
  void validate_positive_pickupLocationWithHoldShelfExpiry() {
    var servicePoint = new ServicePoint()
      .pickupLocation(true)
      .holdShelfExpiryPeriod(new HoldShelfExpiryPeriod());

    assertThatNoException().isThrownBy(() -> validator.validate(servicePoint));
  }

  @ParameterizedTest
  @MethodSource("nonPickupLocations")
  void validate_positive_nonPickupLocationWithoutHoldShelfExpiry(Boolean pickupLocation) {
    var servicePoint = new ServicePoint().pickupLocation(pickupLocation);

    assertThatNoException().isThrownBy(() -> validator.validate(servicePoint));
  }

  @Test
  void validate_negative_pickupLocationWithoutHoldShelfExpiry() {
    var servicePoint = new ServicePoint().pickupLocation(true);

    assertThatThrownBy(() -> validator.validate(servicePoint))
      .isInstanceOf(ValidationException.class)
      .hasMessage(ServicePointValidator.ERR_PICKUP_WITHOUT_HOLD_EXPIRY);
  }

  @ParameterizedTest
  @MethodSource("nonPickupLocations")
  void validate_negative_holdShelfExpiryWithoutPickupLocation(Boolean pickupLocation) {
    var servicePoint = new ServicePoint()
      .pickupLocation(pickupLocation)
      .holdShelfExpiryPeriod(new HoldShelfExpiryPeriod());

    assertThatThrownBy(() -> validator.validate(servicePoint))
      .isInstanceOf(ValidationException.class)
      .hasMessage(ServicePointValidator.ERR_HOLD_EXPIRY_WITHOUT_PICKUP);
  }

  private static Stream<Boolean> nonPickupLocations() {
    return Stream.of(false, null);
  }
}
