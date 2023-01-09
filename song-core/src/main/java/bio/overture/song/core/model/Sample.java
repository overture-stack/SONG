package bio.overture.song.core.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Sample extends Metadata {

  private String sampleId;
  private String specimenId;
  private String submitterSampleId;
  private String sampleType;

  @JsonInclude(JsonInclude.Include.ALWAYS)
  private String matchedNormalSubmitterSampleId;
}
