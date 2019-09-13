package bio.overture.song.server.model.dto;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import bio.overture.song.server.model.analysis.AnalysisTypeId;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(value = NON_NULL)
public class UpdateAnalysisRequest extends DynamicData {

  private AnalysisTypeId analysisType;
}
