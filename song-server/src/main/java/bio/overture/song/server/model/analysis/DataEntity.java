package bio.overture.song.server.model.analysis;

import static bio.overture.song.core.model.enums.AnalysisStates.UNPUBLISHED;
import static bio.overture.song.server.repository.CustomJsonType.CUSTOM_JSON_TYPE_PKG_PATH;

import bio.overture.song.server.model.enums.TableAttributeNames;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;
import javax.persistence.*;
import lombok.*;
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

  @Column(name = TableAttributeNames.ANALYSIS_ID, nullable = false)
  private String analysisId;

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

  /** Sample */
  @Column(name = TableAttributeNames.SAMPLE_ID, nullable = false)
  private String sampleId;

  @Column(name = "SAMPLE_SPECIMEN_ID", nullable = false)
  private String sampleSpecimenId;

  @Column(name = "SAMPLE_SUBMITTER_ID", nullable = false)
  private String sampleSubmitterId;

  @Column(name = "SAMPLE_TYPE", nullable = false)
  private String sampleType;

  @Column(name = TableAttributeNames.MATCHED_NORMAL_SUBMITTER_SAMPLE_ID, nullable = true)
  private String matchedNormalSubmitterSampleId;

  @Column(name = "SAMPLE_INFO")
  @Type(type = CUSTOM_JSON_TYPE_PKG_PATH)
  private final Map<String, Object> sampleInfo = new TreeMap<>();

  /** Specimen */
  @Column(
      name = TableAttributeNames.SPECIMEN_ID,
      updatable = false,
      unique = true,
      nullable = false)
  private String specimenId;

  @Column(name = "SPECIMEN_DONOR_ID", updatable = false, unique = true, nullable = false)
  private String specimenDonorId;

  @Column(name = "SUBMITTER_SPECIMEN_ID", nullable = false)
  private String submitterSpecimenId;

  @Column(name = "SPECIMEN_TYPE", nullable = false)
  private String specimenType;

  @Column(name = TableAttributeNames.TISSUE_SOURCE, nullable = false)
  private String specimenTissueSource;

  @Column(name = TableAttributeNames.TUMOUR_NORMAL_DESIGNATION, nullable = false)
  private String tumourNormalDesignation;

  @Column(name = "SPECIMEN_INFO")
  @Type(type = CUSTOM_JSON_TYPE_PKG_PATH)
  private final Map<String, Object> specimenInfo = new TreeMap<>();

  /** Donor */
  @Column(name = "DONOR_DONOR_ID", updatable = false, unique = true, nullable = false)
  private String donorId;

  @Column(name = "SUBMITTER_DONOR_ID", nullable = false)
  private String submitterDonorId;

  @Column(name = TableAttributeNames.GENDER, nullable = false)
  private String gender;

  @Column(name = "DONOR_STUDY_ID", nullable = false)
  private String donorStudyId;

  @Column(name = "DONOR_INFO")
  @Type(type = CUSTOM_JSON_TYPE_PKG_PATH)
  private final Map<String, Object> donorInfo = new TreeMap<>();
}
