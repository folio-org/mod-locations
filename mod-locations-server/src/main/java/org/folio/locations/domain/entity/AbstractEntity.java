package org.folio.locations.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

@Getter
@Setter
@MappedSuperclass
public abstract class AbstractEntity<I> {

  @Column(name = "created_date")
  private OffsetDateTime createdDate;

  @Column(name = "created_by_user_id")
  private UUID createdByUserId;

  @Column(name = "updated_date")
  private OffsetDateTime updatedDate;

  @Column(name = "updated_by_user_id")
  private UUID updatedByUserId;

  public abstract I getId();

  public abstract void setId(I id);

  @Override
  public final int hashCode() {
    return Objects.hash(getId());
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null) {
      return false;
    }
    Class<?> effectiveClass = o instanceof HibernateProxy proxy
                               ? proxy.getHibernateLazyInitializer().getPersistentClass()
                               : o.getClass();
    Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy
                                  ? proxy.getHibernateLazyInitializer().getPersistentClass()
                                  : this.getClass();
    if (thisEffectiveClass != effectiveClass) {
      return false;
    }
    AbstractEntity<?> that = (AbstractEntity<?>) o;
    return getId() != null && Objects.equals(getId(), that.getId());
  }
}
