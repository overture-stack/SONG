package org.icgc.dcc.song.server.model.analysis;

import lombok.Data;

@Data
public class IdSearchRequest {

  private static final String WILD_CARD = ".*";

  private final String donorId;
  private final String sampleId;
  private final String specimenId;
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
