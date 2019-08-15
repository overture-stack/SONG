package bio.overture.song.server.repository;

import bio.overture.song.server.model.entity.AnalysisSchema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AnalysisSchemaRepository extends JpaRepository<AnalysisSchema, Integer>, JpaSpecificationExecutor<AnalysisSchema> {

  Integer countAllByName(String name);
  Integer countAllByNameAndIdLessThanEqual(String name, Integer id);
  Optional<AnalysisSchema> findByNameAndVersion(String name, Integer version);


}
