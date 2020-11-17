package bio.overture.song.server.model.analysis;

import bio.overture.song.server.model.enums.TableAttributeNames;
import bio.overture.song.server.model.enums.TableNames;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = TableNames.ANALYSIS_STATE_CHANGE)
public class AnalysisStateChange implements Comparable<AnalysisStateChange> {

  @Id
  @Column(name = TableAttributeNames.ID)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @JsonIgnore
  private Integer id;

  @ManyToOne
  @JoinColumn(name = TableAttributeNames.ANALYSIS_ID, nullable = false, updatable = false)
  @JsonIgnore
  private Analysis analysis;

  @Column(name = TableAttributeNames.INITIAL_STATE, updatable = false, nullable = false)
  private String initialState;

  @Column(name = TableAttributeNames.UPDATED_STATE, updatable = false, nullable = false)
  private String updatedState;

  @Column(name = TableAttributeNames.UPDATED_AT, updatable = false, nullable = false)
  private LocalDateTime updatedAt;

  @Override
  public int compareTo(@NotNull AnalysisStateChange o) {
    // Define a natural sort order based on updatedAt time.

    return this.getUpdatedAt().compareTo(o.getUpdatedAt());
  }
}
