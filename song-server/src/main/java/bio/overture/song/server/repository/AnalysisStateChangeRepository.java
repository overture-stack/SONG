package bio.overture.song.server.repository;

import bio.overture.song.server.model.analysis.AnalysisStateChange;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisStateChangeRepository
    extends JpaRepository<AnalysisStateChange, Integer> {}
