package bio.overture.song.server.model.dto;

import bio.overture.song.core.model.AnalysisTypeId;
import bio.overture.song.core.model.DynamicData;
import bio.overture.song.server.model.entity.FileEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
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
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class Payload extends DynamicData {

  private String studyId;
  private AnalysisTypeId analysisType;
  private List<CompositeEntity> samples;
  private List<FileEntity> files;
}
