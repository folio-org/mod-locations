package org.folio.locations.service.crud;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Extension of {@link GetAllContext} that adds an {@code includeRoutingServicePoints} filter,
 * used by the service-point service.
 */
@NullMarked
public class ServicePointFilterContext extends GetAllContext {

  private final @Nullable Boolean includeRoutingServicePoints;

  public ServicePointFilterContext(@Nullable String query, Integer limit, Integer offset,
                                   @Nullable Boolean includeRoutingServicePoints) {
    super(query, limit, offset);
    this.includeRoutingServicePoints = includeRoutingServicePoints;
  }

  public @Nullable Boolean includeRoutingServicePoints() {
    return includeRoutingServicePoints;
  }
}
