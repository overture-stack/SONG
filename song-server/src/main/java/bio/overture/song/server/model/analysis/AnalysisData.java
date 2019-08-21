package bio.overture.song.server.model.analysis;

import static bio.overture.song.server.model.enums.TableAttributeNames.DATA;
import static bio.overture.song.server.model.enums.TableAttributeNames.ID;
import static bio.overture.song.server.repository.CustomJsonType.CUSTOM_JSON_TYPE_PKG_PATH;

import bio.overture.song.server.model.enums.ModelAttributeNames;
import bio.overture.song.server.model.enums.TableNames;
import com.fasterxml.jackson.databind.JsonNode;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Type;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = TableNames.ANALYSIS_DATA)
public class AnalysisData {

  @Id
  @Column(name = ID)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  @OneToOne(
      mappedBy = ModelAttributeNames.ANALYSIS_DATA,
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      optional = false)
  private Analysis2 analysis;

  @NotNull
  @Column(name = DATA)
  @Type(type = CUSTOM_JSON_TYPE_PKG_PATH)
  private JsonNode data;

  public void setAnalysis(Analysis2 a) {
    this.analysis = a;
    a.setAnalysisData(this);
  }
}
