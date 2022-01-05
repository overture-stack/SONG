package bio.overture.song.server.model.analysis;

import static bio.overture.song.core.model.enums.AnalysisStates.UNPUBLISHED;

import bio.overture.song.server.model.enums.TableAttributeNames;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/** Class to map return results of DB function 'get_analysis_state_change' */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisStageChangeJoin implements Serializable, Comparable<AnalysisStageChangeJoin> {

  /** Columns from ANALYSIS table */
  @Id
  @Column(name = TableAttributeNames.ID)
  private String id;

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

  /** Columns from ANALYSIS_STATE_CHANGE table */
  @Id
  @Column(name = "ANALYSIS_STATE_CHANGE_ID")
  private Integer analysisStateChangeId;

  @Column(name = TableAttributeNames.INITIAL_STATE, updatable = false, nullable = false)
  private String initialState;

  @Column(name = TableAttributeNames.UPDATED_STATE, updatable = false, nullable = false)
  private String updatedState;

  @Column(name = "STATE_UPDATED_AT", updatable = false, nullable = false)
  private LocalDateTime stateUpdatedAt;

  @Override
  public int compareTo(@NotNull AnalysisStageChangeJoin o) {
    // Define a natural sort order based on updatedAt time.
    return this.getUpdatedAt().compareTo(o.getUpdatedAt());
  }
}
