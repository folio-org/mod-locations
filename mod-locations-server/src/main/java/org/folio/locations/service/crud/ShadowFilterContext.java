package org.folio.locations.service.crud;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Extension of {@link GetAllContext} that adds an {@code includeShadow} filter,
 * used by institution, campus, library, and location services.
 */
@NullMarked
public class ShadowFilterContext extends GetAllContext {

  private final @Nullable Boolean includeShadow;

  public ShadowFilterContext(@Nullable String query, Integer limit, Integer offset,
                             @Nullable Boolean includeShadow) {
    super(query, limit, offset);
    this.includeShadow = includeShadow;
  }

  public @Nullable Boolean includeShadow() {
    return includeShadow;
  }
}
