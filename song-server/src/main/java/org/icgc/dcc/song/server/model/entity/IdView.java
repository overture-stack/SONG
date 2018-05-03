package org.icgc.dcc.song.server.model.entity;

import jdk.nashorn.internal.ir.annotations.Immutable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.icgc.dcc.song.server.model.enums.ModelAttributeNames;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;
import org.icgc.dcc.song.server.model.enums.TableNames;

import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;

import static org.icgc.dcc.song.server.model.enums.TableAttributeNames.ANALYSIS_ID;
import static org.icgc.dcc.song.server.model.enums.TableAttributeNames.ANALYSIS_STATE;
import static org.icgc.dcc.song.server.model.enums.TableAttributeNames.ANALYSIS_TYPE;
import static org.icgc.dcc.song.server.model.enums.TableAttributeNames.DONOR_ID;
import static org.icgc.dcc.song.server.model.enums.TableAttributeNames.OBJECT_ID;
import static org.icgc.dcc.song.server.model.enums.TableAttributeNames.SAMPLE_ID;
import static org.icgc.dcc.song.server.model.enums.TableAttributeNames.SPECIMEN_ID;
import static org.icgc.dcc.song.server.model.enums.TableAttributeNames.STUDY_ID;

//NOTE: Since the '~*' regex evaluator does not exist in HQL, need to create a native postgres call
@NamedNativeQueries({
    @NamedNativeQuery(
        name = IdView.ID_SEARCH_QUERY_NAME,
        query = "SELECT DISTINCT "+ STUDY_ID+", "+ ANALYSIS_ID+", "+ANALYSIS_STATE+", "+ANALYSIS_TYPE
            + " FROM IdView WHERE "+STUDY_ID+" = :"+ ModelAttributeNames.STUDY_ID
            +" AND "+ DONOR_ID+" ~* :"+ModelAttributeNames.DONOR_ID
            +" AND "+SPECIMEN_ID+" ~* :"+ModelAttributeNames.SPECIMEN_ID
            +" AND "+SAMPLE_ID+" ~* :"+ModelAttributeNames.SAMPLE_ID
            +" AND "+ OBJECT_ID+" ~* :"+ModelAttributeNames.OBJECT_ID,
        resultSetMapping = IdView.ID_VIEW_DTO)
})
@SqlResultSetMappings({
    @SqlResultSetMapping(
        name = IdView.ID_VIEW_DTO,
        classes = @ConstructorResult(
            targetClass = IdView.IdViewProjection.class,
            columns = {
                @ColumnResult(name = STUDY_ID),
                @ColumnResult(name = ANALYSIS_ID),
                @ColumnResult(name = ANALYSIS_STATE),
                @ColumnResult(name = ANALYSIS_TYPE)
            }
        ))
})
@Entity
@Table(name = TableNames.ID_VIEW)
@Immutable
@Data
public class IdView {
  public static final String ID_VIEW_DTO = "idViewDTO";
  public static final String ID_SEARCH_QUERY_NAME = "idSearch";

  @Id
  @Column(name = OBJECT_ID)
  private String objectId;

  @Column(name = DONOR_ID)
  private String donorId;

  @Column(name = TableAttributeNames.SPECIMEN_ID)
  private String specimenId;

  @Column(name = TableAttributeNames.SAMPLE_ID)
  private String sampleId;

  @Column(name = STUDY_ID)
  private String studyId;

  @Column(name = ANALYSIS_ID)
  private String analysisId;

  @Column(name = ANALYSIS_TYPE)
  private String analysisType;

  @Column(name = ANALYSIS_STATE)
  private String analysisState;

  @Data
  @AllArgsConstructor
  @RequiredArgsConstructor
  public static class IdViewProjection{
    private String studyId;
    private String analysisId;
    private String analysisState;
    private String analysisType;
  }

}
