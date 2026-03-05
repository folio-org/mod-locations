package org.folio.locations.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServicePointStaffSlipId implements Serializable {

  @Column(name = "service_point_id")
  private UUID servicePointId;

  @Column(name = "staff_slip_id")
  private UUID staffSlipId;
}
