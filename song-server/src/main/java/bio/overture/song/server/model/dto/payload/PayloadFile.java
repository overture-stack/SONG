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
public class PayloadFile extends Metadata {
  private String fileName;
  private Long fileSize;
  private String fileType;
  private String fileMd5sum;
  private String fileAccess;
}
