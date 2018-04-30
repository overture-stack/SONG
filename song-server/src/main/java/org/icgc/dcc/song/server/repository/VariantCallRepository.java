package org.icgc.dcc.song.server.repository;

import org.icgc.dcc.song.server.model.experiment.VariantCall;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VariantCallRepository extends JpaRepository<VariantCall, String> {

}
