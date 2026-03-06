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
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = ServicePointUserEntity.SERVICE_POINT_USER_TABLE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicePointUserEntity extends AbstractEntity<UUID> {

  public static final String SERVICE_POINT_USER_TABLE = "service_point_user";
  public static final String SERVICE_POINT_USER_SERVICE_POINT_TABLE = "service_point_user_service_point";

  @Id
  @Column(updatable = false, nullable = false)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "default_service_point_id")
  private UUID defaultServicePointId;

  @Builder.Default
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
    name = SERVICE_POINT_USER_SERVICE_POINT_TABLE,
    joinColumns = @JoinColumn(name = "service_point_user_id")
  )
  @Column(name = "service_point_id")
  private List<UUID> servicePointsIds = new ArrayList<>();
}
