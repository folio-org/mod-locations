package org.folio.locations.domain.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = LocationEntity.LOCATION_TABLE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationEntity extends AbstractEntity<UUID> {

  public static final String LOCATION_TABLE = "location";
  public static final String LOCATION_SERVICE_POINT_TABLE = "location_service_point";

  @Id
  @Column(updatable = false, nullable = false)
  private UUID id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String code;

  @Column
  private String description;

  @Column(name = "discovery_display_name")
  private String discoveryDisplayName;

  @Column(name = "is_active")
  private Boolean isActive;

  @Column(name = "institution_id", nullable = false)
  private UUID institutionId;

  @Column(name = "campus_id", nullable = false)
  private UUID campusId;

  @Column(name = "library_id", nullable = false)
  private UUID libraryId;

  @Builder.Default
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "json")
  private Map<String, Object> details = new HashMap<>();

  @Column(name = "primary_service_point", nullable = false)
  private UUID primaryServicePoint;

  @Column(name = "is_floating_collection")
  private Boolean isFloatingCollection;

  @Column(name = "is_shadow")
  private Boolean isShadow;

  @Builder.Default
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = LOCATION_SERVICE_POINT_TABLE, joinColumns = @JoinColumn(name = "location_id"))
  @Column(name = "service_point_id")
  private List<UUID> servicePointIds = new ArrayList<>();
}
