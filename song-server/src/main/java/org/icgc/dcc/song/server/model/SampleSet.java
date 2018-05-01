package org.icgc.dcc.song.server.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.icgc.dcc.song.server.model.enums.TableNames;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = TableNames.SAMPLESET)
@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class SampleSet {

  @EmbeddedId
  private SampleSetPK sampleSetPK;

}
