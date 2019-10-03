package bio.overture.song.core.model;

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
public class Specimen extends Metadata {

  private String specimenId;
  private String donorId;
  private String specimenSubmitterId;
  private String specimenClass;
  private String specimenType;
}
