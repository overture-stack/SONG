package org.icgc.dcc.sodalite.server.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SpecimenClass {

  NORMAL("Normal"),
  TUMOUR("Tumour"),
  ADJACENT_NORMAL("Adjacent normal");

  private final String value;
  private final static Map<String, SpecimenClass> CONSTANTS = new HashMap<String, SpecimenClass>();

  static {
      for (SpecimenClass c: values()) {
          CONSTANTS.put(c.value, c);
      }
  }

  private SpecimenClass(String value) {
      this.value = value;
  }

  @Override
  public String toString() {
      return this.value;
  }

  @JsonValue
  public String value() {
      return this.value;
  }

  @JsonCreator
  public static SpecimenClass fromValue(String value) {
      SpecimenClass constant = CONSTANTS.get(value);
      if (constant == null) {
          throw new IllegalArgumentException(value);
      } else {
          return constant;
      }
  }
}
