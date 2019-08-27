package bio.overture.song.server.model.dto.payload;

import bio.overture.song.server.model.Metadata;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PayloadSpecimen extends Metadata {
  private String specimenSubmitterId;
  private String specimenClass;
  private String specimenType;
}
