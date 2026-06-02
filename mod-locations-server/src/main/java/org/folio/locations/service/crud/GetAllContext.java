package org.folio.locations.service.crud;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Base context for {@code getAll} operations, carrying common pagination and query parameters.
 */
@NullMarked
public class GetAllContext {

  private final @Nullable String query;
  private final Integer limit;
  private final Integer offset;

  public GetAllContext(@Nullable String query, Integer limit, Integer offset) {
    this.query = query;
    this.limit = limit;
    this.offset = offset;
  }

  public @Nullable String query() {
    return query;
  }

  public Integer limit() {
    return limit;
  }

  public Integer offset() {
    return offset;
  }
}
