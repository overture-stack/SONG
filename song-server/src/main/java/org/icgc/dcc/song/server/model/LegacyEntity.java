package org.icgc.dcc.song.server.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegacyEntity {

  private String id;
  private String gnosId;
  private String fileName;
  private String projectCode;
  private String access;

}