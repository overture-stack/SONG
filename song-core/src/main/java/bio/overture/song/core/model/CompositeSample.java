package bio.overture.song.core.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CompositeSample extends Sample {

  private Specimen specimen;
  private Donor donor;
}
