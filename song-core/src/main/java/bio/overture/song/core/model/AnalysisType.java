package bio.overture.song.core.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisType {

  @NotNull private String name;
  @NotNull private Integer version;
  private LocalDateTime createdAt;
  private AnalysisTypeOptions options;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private JsonNode schema;

  public List<String> getFileTypes() {
    if (this.options == null) {
      return new ArrayList<String>();
    }
    if (this.options.getFileTypes() == null) {
      return new ArrayList<String>();
    }
    return this.options.getFileTypes();
  }

  public List<ExternalValidation> getExternalValidations() {
    if (this.options == null) {
      return new ArrayList<ExternalValidation>();
    }
    if (this.options.getExternalValidations() == null) {
      return new ArrayList<ExternalValidation>();
    }
    return this.options.getExternalValidations();
  }
}
