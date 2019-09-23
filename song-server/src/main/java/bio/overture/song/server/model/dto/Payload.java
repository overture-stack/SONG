package bio.overture.song.server.model.dto;

import bio.overture.song.core.model.DynamicData;
import bio.overture.song.core.model.AnalysisTypeId;
import bio.overture.song.server.model.entity.FileEntity;
import bio.overture.song.server.model.entity.composites.CompositeEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class Payload extends DynamicData {

  private String study;
  private String analysisId;
  private AnalysisTypeId analysisType;
  private List<CompositeEntity> sample;
  private List<FileEntity> file;
}
