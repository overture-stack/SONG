package org.icgc.dcc.song.server.model;

import lombok.Data;
import lombok.NonNull;
import lombok.val;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
public class SampleSetPK implements Serializable{

  @Column(name = TableAttributeNames.ANALYSIS_ID, nullable = false)
  private String analysisId;

  @Column(name = TableAttributeNames.SAMPLE_ID, nullable = false)
  private String sampleId;

  public static SampleSetPK createSampleSetPK(@NonNull String analysisId, @NonNull String sampleId){
    val s = new SampleSetPK();
    s.setAnalysisId(analysisId);
    s.setSampleId(sampleId);
    return s;
  }

}
