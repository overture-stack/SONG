package bio.overture.song.server.model;

import bio.overture.song.server.model.enums.TableAttributeNames;
import bio.overture.song.server.model.enums.TableNames;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Map;

import static bio.overture.song.server.repository.CustomJsonType.CUSTOM_JSON_TYPE_PKG_PATH;

@Entity
@Table(name = TableNames.EXPERIMENT)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperimentSchema {

  @Id
  @Column(name = TableAttributeNames.ANALYSIS_TYPE)
  private String analysisType;

  @NotNull
  @Type(type = CUSTOM_JSON_TYPE_PKG_PATH)
  @Column(name = TableAttributeNames.SCHEMA)
  private Map<String, Object> schema;

}
