package org.icgc.dcc.song.server.model.analysis;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;
import org.icgc.dcc.song.server.model.enums.TableNames;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = TableNames.ANALYSIS)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Analysis extends AbstractAnalysis {

  @Column(name = TableAttributeNames.TYPE, nullable = false)
  private String analysisType;

  @Override
  public void setWith(AbstractAnalysis a) {
    super.setWith(a);
    setAnalysisType(a.getAnalysisType());
  }

}
