package org.icgc.dcc.sodalite.server.model;

import lombok.val;

public abstract class AbstractEntity implements Entity {

  public abstract void propagateKeys();

  @Override
  public int hashCode() {
    val result = 1;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    return true;
  }

}
