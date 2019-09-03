package bio.overture.song.server.model.entity;

import bio.overture.song.server.model.analysis.Analysis;
import bio.overture.song.server.model.enums.ModelAttributeNames;
import bio.overture.song.server.model.enums.TableNames;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Type;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Set;

import static org.assertj.core.util.Sets.newHashSet;
import static bio.overture.song.server.model.enums.TableAttributeNames.ID;
import static bio.overture.song.server.model.enums.TableAttributeNames.NAME;
import static bio.overture.song.server.model.enums.TableAttributeNames.SCHEMA;
import static bio.overture.song.server.model.enums.TableAttributeNames.VERSION;
import static bio.overture.song.server.repository.CustomJsonType.CUSTOM_JSON_TYPE_PKG_PATH;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = TableNames.ANALYSIS_SCHEMA)
public class AnalysisSchema {

  @Id
  @Column(name = ID)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = VERSION)
  private Integer version;

  @NotNull
  @Column(name = NAME)
  private String name;

  @NotNull
  @Column(name = SCHEMA)
  @Type(type = CUSTOM_JSON_TYPE_PKG_PATH)
  private JsonNode schema;

  @JsonIgnore
  @Builder.Default
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @OneToMany(
      mappedBy = ModelAttributeNames.ANALYSIS_SCHEMA,
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private Set<Analysis> analyses = newHashSet();

  public void associateAnalysis(Analysis a) {
    this.analyses.add(a);
    a.setAnalysisSchema(this);
  }

  public void disassociateAnalysis(Analysis a){
    this.analyses.removeIf(x -> x.getAnalysisId().equals(a.getAnalysisId()));
    a.setAnalysisSchema(null);
  }
}
