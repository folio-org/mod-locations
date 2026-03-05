package org.folio.locations.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "service_point")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "staffSlips")
public class ServicePointEntity extends AbstractEntity<UUID> {

  @Id
  @Column(updatable = false, nullable = false)
  private UUID id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String code;

  @Column(name = "discovery_display_name", nullable = false)
  private String discoveryDisplayName;

  @Column
  private String description;

  @Column(name = "shelving_lag_time")
  private Integer shelvingLagTime;

  @Column(name = "pickup_location")
  private Boolean pickupLocation;

  @Column(name = "hold_shelf_expiry_period_interval_id")
  private String holdShelfExpiryPeriodIntervalId;

  @Column(name = "hold_shelf_expiry_period_duration")
  private Integer holdShelfExpiryPeriodDuration;

  @Column(name = "hold_shelf_closed_library_date_management")
  private String holdShelfClosedLibraryDateManagement;

  @Column(name = "default_check_in_action_for_use_at_location")
  private String defaultCheckInActionForUseAtLocation;

  @Column(name = "ecs_request_routing")
  private Boolean ecsRequestRouting;

  @Builder.Default
  @OneToMany(mappedBy = "servicePoint", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  private List<ServicePointStaffSlipEntity> staffSlips = new ArrayList<>();

  @Column(name = "created_date")
  private OffsetDateTime createdDate;

  @Column(name = "created_by_user_id")
  private UUID createdByUserId;

  @Column(name = "updated_date")
  private OffsetDateTime updatedDate;

  @Column(name = "updated_by_user_id")
  private UUID updatedByUserId;
}
