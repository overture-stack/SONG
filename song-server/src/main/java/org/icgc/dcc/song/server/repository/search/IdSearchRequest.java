package org.icgc.dcc.song.server.repository.search;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class IdSearchRequest {

  private static final String WILD_CARD = ".*";

  @ApiModelProperty(notes = "regex pattern. Default is wildcard")
  private final String donorId;

  @ApiModelProperty(notes = "regex pattern. Default is wildcard")
  private final String sampleId;

  @ApiModelProperty(notes = "regex pattern. Default is wildcard")
  private final String specimenId;

  @ApiModelProperty(notes = "regex pattern. Default is wildcard")
  private final String fileId;

  public static IdSearchRequest createIdSearchRequest(
      String donorId, String sampleId, String specimenId, String fileId) {
    return new IdSearchRequest(
        getGlobPattern(donorId),
        getGlobPattern(sampleId),
        getGlobPattern(specimenId),
        getGlobPattern(fileId)
    );
  }

  public String getDonorId(){
    return getGlobPattern(this.donorId);
  }
  public String getSampleId(){
    return getGlobPattern(this.sampleId);
  }
  public String getSpecimenId(){
    return getGlobPattern(this.specimenId);
  }
  public String getFileId(){
    return getGlobPattern(this.fileId);
  }

  private static String getGlobPattern(String opt){
    return opt == null ?  WILD_CARD : opt;
  }

}
