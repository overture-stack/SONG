package org.icgc.dcc.sodalite.server.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SampleType {

  DNA("DNA"),
  FFPE_DNA("FFPE DNA"),
  AMPLIFIED_DNA("Amplified DNA"),
  RNA("RNA"),
  TOTAL_RNA("Total RNA"),
  FFPE_RNA("FFPE RNA");
  private final String value;
  private final static Map<String, SampleType> CONSTANTS = new HashMap<String, SampleType>();

  static {
      for (SampleType c: values()) {
          CONSTANTS.put(c.value, c);
      }
  }

  private SampleType(String value) {
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
  public static SampleType fromValue(String value) {
      SampleType constant = CONSTANTS.get(value);
      if (constant == null) {
          throw new IllegalArgumentException(value);
      } else {
          return constant;
      }
  }

}
