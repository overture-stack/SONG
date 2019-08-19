package bio.overture.song.server.repository;

import bio.overture.song.server.model.analysis.AnalysisData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisDataRepository extends JpaRepository<AnalysisData, Integer> {

}
