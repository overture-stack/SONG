package bio.overture.song.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDTO extends Metadata implements File {

  private String objectId;
  private String studyId;
  private String analysisId;
  private String fileName;
  private String fileType;
  private String fileMd5sum;
  private Long fileSize;
  private String fileAccess;
}
