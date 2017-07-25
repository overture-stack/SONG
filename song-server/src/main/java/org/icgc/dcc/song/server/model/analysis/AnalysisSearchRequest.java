package org.icgc.dcc.song.server.model.analysis;

import lombok.Data;

@Data
public class AnalysisSearchRequest {

  private static final String WILD_CARD = "%";

  private final String donorId;
  private final String sampleId;
  private final String specimenId;
  private final String fileId;

  public static AnalysisSearchRequest createAnalysisSearchRequest(
      String donorId, String sampleId, String specimenId, String fileId) {
    return new AnalysisSearchRequest(
        procOpt(donorId),
        procOpt(sampleId),
        procOpt(specimenId),
        procOpt(fileId)
    );
  }

  public String getDonorId(){
    return procOpt(this.donorId);
  }
  public String getSampleId(){
    return procOpt(this.sampleId);
  }
  public String getSpecimenId(){
    return procOpt(this.specimenId);
  }
  public String getFileId(){
    return procOpt(this.fileId);
  }

  private static String procOpt(String opt){
    return opt == null ?  WILD_CARD : opt;
  }

}
