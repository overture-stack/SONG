package bio.overture.song.server.repository;

import bio.overture.song.server.model.entity.AnalysisType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnalysisTypeRepository extends JpaRepository<AnalysisType, Integer>{

  Optional<AnalysisType> findFirstByNameOrderByIdDesc(String name);
  List<AnalysisType> findAllByNameOrderByIdDesc(String name);
  Integer countAllByName(String name);

}
