package org.icgc.dcc.song.server.repository;

import org.icgc.dcc.song.server.model.SampleSet;
import org.icgc.dcc.song.server.model.SampleSetPK;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SampleSetRepository extends JpaRepository<SampleSet, SampleSetPK>{

  void deleteAllBySampleSetPK_AnalysisId(String analysisId);
  List<SampleSet> findAllBySampleSetPK_AnalysisId(String analysisId);

}
