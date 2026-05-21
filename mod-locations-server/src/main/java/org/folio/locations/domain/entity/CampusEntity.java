package org.folio.locations.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "campus")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampusEntity extends AbstractEntity<UUID> {

  public static final String CAMPUS_TABLE = "campus";

  @Id
  @Column(updatable = false, nullable = false)
  private UUID id;

  private String name;

  private String code;

  @Column(name = "is_shadow")
  private Boolean isShadow;

  @Column(name = "institution_id")
  private UUID institutionId;
}
