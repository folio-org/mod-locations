package org.folio.locations.domain.entity;

import java.util.Objects;
import org.hibernate.proxy.HibernateProxy;

public abstract class AbstractEntity<I> {

  public abstract I getId();

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
    Class<?> oEffectiveClass = o instanceof HibernateProxy proxy
                               ? proxy.getHibernateLazyInitializer().getPersistentClass()
                               : o.getClass();
    Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy
                                  ? proxy.getHibernateLazyInitializer().getPersistentClass()
                                  : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) {
      return false;
    }
    ServicePointEntity that = (ServicePointEntity) o;
    return getId() != null && Objects.equals(getId(), that.getId());
  }
}
