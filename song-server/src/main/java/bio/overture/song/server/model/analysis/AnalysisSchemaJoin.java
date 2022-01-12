package bio.overture.song.server.model.analysis;

import static bio.overture.song.core.model.enums.AnalysisStates.UNPUBLISHED;
import static bio.overture.song.server.model.enums.TableAttributeNames.*;
import static bio.overture.song.server.repository.CustomJsonType.CUSTOM_JSON_TYPE_PKG_PATH;

import bio.overture.song.server.model.enums.TableAttributeNames;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

/** Class to map the return results of DB function get_analysis_schema. */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisSchemaJoin {

  @Id
  @Column(name = TableAttributeNames.ID, updatable = false, unique = true, nullable = false)
  private String analysisId;

  @Column(name = TableAttributeNames.STUDY_ID, nullable = false)
  private String studyId;

  @Column(name = TableAttributeNames.STATE, nullable = false)
  private String analysisState = UNPUBLISHED.name();

  @Column(name = TableAttributeNames.ANALYSIS_SCHEMA_ID, nullable = false)
  private Integer analysisSchemaId;

  @Column(name = VERSION)
  private Integer version;

  @NotNull
  @Column(name = NAME)
  private String name;

  @Column(name = "ANALYSIS_SCHEMA_CREATED_AT")
  @CreationTimestamp
  private LocalDateTime createdAt;

  @NotNull
  @Column(name = SCHEMA)
  @Type(type = CUSTOM_JSON_TYPE_PKG_PATH)
  private JsonNode schema;
}
