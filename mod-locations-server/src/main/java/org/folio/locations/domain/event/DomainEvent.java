package org.folio.locations.domain.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.folio.locations.domain.type.ResourceType;

/**
 * Generic domain event published to Kafka on any state change.
 *
 * <p>Consumers can correlate old/new state, identify who made the change, and filter
 * by resource type without deserializing the payload.
 *
 * @param <T> the DTO type of the resource that changed
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DomainEvent<T> {

  /**
   * Unique identifier for this event — safe for deduplication.
   */
  @NotNull
  private UUID eventId;

  /**
   * Epoch milliseconds when the domain action occurred.
   */
  @NotNull
  private Long eventTs;

  /**
   * Discriminator that allows routing/filtering without inspecting the payload.
   */
  @NotNull
  private ResourceType resourceType;

  /**
   * The type of mutation that produced this event.
   */
  @NotNull
  private DomainEventType type;

  /**
   * Tenant context in which the change happened (maps to X-Okapi-Tenant).
   */
  @NotNull
  private String tenant;

  /**
   * Identifier of the resource that changed — useful for DELETE events where {@code new} is null.
   */
  @NotNull
  private UUID resourceId;

  /**
   * User who initiated the change. {@code null} for system-generated events.
   */
  private UUID userId;

  /**
   * State of the resource after the change. {@code null} for DELETE events.
   */
  @JsonProperty("new")
  @Nullable
  private T newResource;

  /**
   * State of the resource before the change. {@code null} for CREATE events.
   */
  @JsonProperty("old")
  @Nullable
  private T oldResource;
}
