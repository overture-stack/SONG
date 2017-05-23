package org.icgc.dcc.sodalite.server.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.val;

public enum AnalysisType {
  SEQUENCING_READ("sequencingRead"), VARIANT_CALL("variantCall"), TUMOUR_NORMAL_PAIR("tumourNormalPair");

  private final String value;
  private final static Map<String, AnalysisType> CONSTANTS = new HashMap<String, AnalysisType>();

  static {
    for (val c : values()) {
      CONSTANTS.put(c.value, c);
    }
  }

  private AnalysisType(String value) {
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
  public static AnalysisType fromValue(String value) {
    val constant = CONSTANTS.get(value);
    if (constant == null) {
      throw new IllegalArgumentException(value);
    } else {
      return constant;
    }
  }

  public static boolean isAnalysisType(String value) {
    try {
      AnalysisType.fromValue(value);
    } catch (IllegalArgumentException iae) {
      return false;
    }
    return true;
  }

}
