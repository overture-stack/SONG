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

package bio.overture.song.server.model.analysis;

import static bio.overture.song.core.model.enums.AnalysisStates.*;
import static bio.overture.song.core.utils.JsonUtils.toMap;
import static bio.overture.song.server.service.AnalysisTypeService.resolveAnalysisTypeId;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Sets.newHashSet;

import bio.overture.song.core.model.AnalysisTypeId;
import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.server.model.entity.AnalysisSchema;
import bio.overture.song.server.model.entity.FileEntity;
import bio.overture.song.server.model.entity.composites.CompositeEntity;
import bio.overture.song.server.model.enums.ModelAttributeNames;
import bio.overture.song.server.model.enums.TableAttributeNames;
import bio.overture.song.server.model.enums.TableNames;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = TableNames.ANALYSIS)
public class Analysis {

  @Id
  @Column(name = TableAttributeNames.ID, updatable = false, unique = true, nullable = false)
  private String analysisId;

  @Column(name = TableAttributeNames.STUDY_ID, nullable = false)
  private String studyId;

  @Column(name = TableAttributeNames.STATE, nullable = false)
  private String analysisState = UNPUBLISHED.name();

  @Column(name = TableAttributeNames.CREATED_AT, nullable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;

  @Column(name = TableAttributeNames.UPDATED_AT, nullable = false)
  @UpdateTimestamp
  private LocalDateTime updatedAt;

  @Transient private LocalDateTime firstPublishedAt;
  @Transient private LocalDateTime publishedAt;

  @NotNull
  @JsonIgnore
  @ManyToOne(fetch = FetchType.EAGER)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @JoinColumn(name = TableAttributeNames.ANALYSIS_SCHEMA_ID, nullable = false)
  private AnalysisSchema analysisSchema;

  @OneToOne
  @JoinColumn(name = TableAttributeNames.ANALYSIS_DATA_ID)
  @JsonIgnore
  private AnalysisData analysisData;

  @NotNull
  @Builder.Default
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @OneToMany(
      mappedBy = ModelAttributeNames.ANALYSIS,
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.EAGER)
  private Set<AnalysisStateChange> analysisStateHistory = newHashSet();

  @Transient private List<CompositeEntity> samples;

  @Transient private List<FileEntity> files;

  // TODO: need to remove this, and replace anything that needs this with Payload object
  public AnalysisTypeId getAnalysisType() {
    return resolveAnalysisTypeId(analysisSchema);
  }

  @SneakyThrows
  @JsonAnyGetter
  public Map<String, Object> getData() {
    return toMap(JsonUtils.toJson(analysisData.getData()));
  }

  public void setAnalysisState(String state) {
    this.analysisState = resolveAnalysisState(state).toString();
  }

  public void populatePublishTimes() {
    val history = this.analysisStateHistory;

    if (!history.isEmpty()) {
      val publishHistory =
          history.stream()
              .filter(stateChange -> stateChange.getUpdatedState().equals(PUBLISHED.name()))
              .map(stateChange -> stateChange.getUpdatedAt())
              .collect(toImmutableList());

      if (!publishHistory.isEmpty()) {
        val firstPublish = publishHistory.stream().min(LocalDateTime::compareTo).get();
        val lastPublish = publishHistory.stream().max(LocalDateTime::compareTo).get();
        this.setFirstPublishedAt(firstPublish);
        this.setPublishedAt(lastPublish);
      }
    }
  }
}
