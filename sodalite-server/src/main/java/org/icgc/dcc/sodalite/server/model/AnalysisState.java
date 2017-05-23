package org.icgc.dcc.sodalite.server.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AnalysisState {

  RECEIVED("RECEIVED"), ERROR("ERROR"), SYSTEM_ERROR("SYSTEM ERROR"), VALIDATED("VALIDATED"), READY_FOR_UPLOAD("READY FOR UPLOAD"), 
  READY_FOR_PUBLISH("READY FOR PUBLISH"), PUBLISHED("PUBLISHED"), SUPPRESSED("SUPPRESSED");

  private final String value;
  private final static Map<String, AnalysisState> CONSTANTS = new HashMap<String, AnalysisState>();

  static {
    for (AnalysisState c : values()) {
      CONSTANTS.put(c.value, c);
    }
  }

  private AnalysisState(String value) {
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
  public static AnalysisState fromValue(String value) {
    AnalysisState constant = CONSTANTS.get(value);
    if (constant == null) {
      throw new IllegalArgumentException(value);
    } else {
      return constant;
    }
  }

}
