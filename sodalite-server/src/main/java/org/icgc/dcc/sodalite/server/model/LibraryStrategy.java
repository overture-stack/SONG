package org.icgc.dcc.sodalite.server.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.val;

public enum LibraryStrategy {

  WGS("WGS"), WXS("WXS"), RNA_SEQ("RNA-Seq"), CH_IP_SEQ("ChIP-Seq"), MI_RNA_SEQ("miRNA-Seq"), BISULFITE_SEQ("Bisulfite-Seq"), VALIDATION("Validation"), AMPLICON("Amplicon"), OTHER("Other");

  private final String value;
  private final static Map<String, LibraryStrategy> CONSTANTS = new HashMap<String, LibraryStrategy>();

  static {
    for (val c : values()) {
      CONSTANTS.put(c.value, c);
    }
  }

  private LibraryStrategy(String value) {
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
  public static LibraryStrategy fromValue(String value) {
    val constant = CONSTANTS.get(value);
    if (constant == null) {
      throw new IllegalArgumentException(value);
    } else {
      return constant;
    }
  }
}
