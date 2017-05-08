package org.icgc.dcc.sodalite.server.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DonorGender {

  MALE("male"), FEMALE("female"), UNSPECIFIED("unspecified");

  private final String value;
  private final static Map<String, DonorGender> CONSTANTS = new HashMap<String, DonorGender>();

  static {
    for (DonorGender c : values()) {
      CONSTANTS.put(c.value, c);
    }
  }

  private DonorGender(String value) {
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
  public static DonorGender fromValue(String value) {
    DonorGender constant = CONSTANTS.get(value);
    if (constant == null) {
      throw new IllegalArgumentException(value);
    } else {
      return constant;
    }
  }

}
