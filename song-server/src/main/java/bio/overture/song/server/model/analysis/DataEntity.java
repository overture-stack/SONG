package bio.overture.song.server.model.analysis;

import static bio.overture.song.core.model.enums.AnalysisStates.UNPUBLISHED;
import static bio.overture.song.server.repository.CustomJsonType.CUSTOM_JSON_TYPE_PKG_PATH;

import bio.overture.song.server.model.enums.TableAttributeNames;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

/** Class to map return join results of DB function 'get_analysis' */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataEntity implements Serializable {

  /** Analysis */
  @Id
  @Column(name = TableAttributeNames.ID, updatable = false, unique = true, nullable = false)
  private String id;

  @Column(name = TableAttributeNames.STUDY_ID, nullable = false)
  private String studyId;

  @Column(name = TableAttributeNames.STATE, nullable = false)
  @Builder.Default
  private String analysisState = UNPUBLISHED.name();

  @Column(name = TableAttributeNames.CREATED_AT, nullable = false)
  private LocalDateTime createdAt;

  @Column(name = TableAttributeNames.UPDATED_AT, nullable = false)
  private LocalDateTime updatedAt;

  /** File */
  @Id
  @Column(name = "file_id", nullable = false)
  private String fileId;

  @Column(name = "file_study_id", nullable = false)
  private String fileStudyId;

  @Column(name = TableAttributeNames.NAME, nullable = false)
  private String fileName;

  @Column(name = TableAttributeNames.SIZE, nullable = false)
  private Long fileSize;

  @Column(name = "file_type", nullable = false)
  private String fileType;

  @Column(name = TableAttributeNames.MD5, nullable = false)
  private String fileMd5sum;

  @Column(name = TableAttributeNames.ACCESS, nullable = false)
  private String fileAccess;

  @Column(name = TableAttributeNames.DATA_TYPE, nullable = false)
  private String dataType;

  @Column(name = TableAttributeNames.INFO)
  @Type(type = CUSTOM_JSON_TYPE_PKG_PATH)
  private final Map<String, Object> fileInfo = new TreeMap<>();
}
