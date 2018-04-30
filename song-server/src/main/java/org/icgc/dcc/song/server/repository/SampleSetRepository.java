package org.icgc.dcc.song.server.repository;

import org.icgc.dcc.song.server.model.SampleSet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SampleSetRepository extends JpaRepository<SampleSet, String>{

}
