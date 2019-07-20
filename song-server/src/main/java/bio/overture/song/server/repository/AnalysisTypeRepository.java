package bio.overture.song.server.repository;

import bio.overture.song.server.model.entity.AnalysisType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AnalysisTypeRepository extends JpaRepository<AnalysisType, UUID>{

  Optional<AnalysisType> findByNameOrderByVersionDesc(String name);

}
