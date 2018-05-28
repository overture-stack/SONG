package org.icgc.dcc.song.server.model.legacy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LegacyDto implements Legacy {

  private String id;
  private String gnosId;
  private String fileName;
  private String projectCode;
  private String access;

}
