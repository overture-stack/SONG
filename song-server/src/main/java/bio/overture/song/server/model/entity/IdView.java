/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package bio.overture.song.server.model.entity;

import bio.overture.song.server.model.enums.ModelAttributeNames;
import bio.overture.song.server.model.enums.TableAttributeNames;
import bio.overture.song.server.model.enums.TableNames;
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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Immutable;

import static bio.overture.song.server.model.enums.TableAttributeNames.*;

// NOTE: Since the '~*' regex evaluator does not exist in HQL, need to create a native postgres call
@NamedNativeQueries({
    @NamedNativeQuery(
        name = IdView.ID_SEARCH_QUERY_NAME,
        query =
            "SELECT DISTINCT "
                + STUDY_ID
                + ", "
                + ANALYSIS_ID
                + ", "
                + ANALYSIS_STATE
                + " FROM IdView WHERE "
                + STUDY_ID
                + " = :"
                + ModelAttributeNames.STUDY_ID
                + " AND "
                + DONOR_ID
                + " ~* :"
                + ModelAttributeNames.DONOR_ID
                + " AND "
                + SPECIMEN_ID
                + " ~* :"
                + ModelAttributeNames.SPECIMEN_ID
                + " AND "
                + SAMPLE_ID
                + " ~* :"
                + ModelAttributeNames.SAMPLE_ID
                + " AND "
                + OBJECT_ID
                + " ~* :"
                + ModelAttributeNames.OBJECT_ID
                + " AND "
                + SUBMITTER_SAMPLE_ID
                + " ~* :"
                + ModelAttributeNames.SAMPLE_SUBMITTER_ID
                + " AND "
                + SUBMITTER_DONOR_ID
                + " ~* :"
                + ModelAttributeNames.DONOR_SUBMITTER_ID
                + " AND "
                + SUBMITTER_SPECIMEN_ID
                + " ~* :"
                + ModelAttributeNames.SPECIMEN_SUBMITTER_ID,
        resultSetMapping = IdView.ID_VIEW_DTO)
})
@SqlResultSetMappings({
  @SqlResultSetMapping(
      name = IdView.ID_VIEW_DTO,
      classes =
          @ConstructorResult(
              targetClass = IdView.IdViewProjection.class,
              columns = {
                @ColumnResult(name = STUDY_ID),
                @ColumnResult(name = ANALYSIS_ID),
                @ColumnResult(name = ANALYSIS_STATE)
              }))
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

  @Column(name = ANALYSIS_STATE)
  private String analysisState;

  @Data
  @AllArgsConstructor
  @RequiredArgsConstructor
  public static class IdViewProjection {
    private String studyId;
    private String analysisId;
    private String analysisState;
  }
}
