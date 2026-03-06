package org.folio.locations.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = ServicePointStaffSlipEntity.SERVICE_POINT_STAFF_SLIP_TABLE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "servicePoint")
public class ServicePointStaffSlipEntity extends AbstractEntity<ServicePointStaffSlipId> {

  public static final String SERVICE_POINT_STAFF_SLIP_TABLE = "service_point_staff_slip";

  @EmbeddedId
  private ServicePointStaffSlipId id;

  @MapsId("servicePointId")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "service_point_id")
  private ServicePointEntity servicePoint;

  @Column(name = "print_by_default", nullable = false)
  private Boolean printByDefault;

  public UUID getStaffSlipId() {
    return id != null ? id.getStaffSlipId() : null;
  }
}
