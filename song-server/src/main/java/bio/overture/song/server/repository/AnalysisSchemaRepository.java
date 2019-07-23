package bio.overture.song.server.repository;

import bio.overture.song.server.model.entity.AnalysisSchema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnalysisSchemaRepository extends JpaRepository<AnalysisSchema, Integer>{

  Page<AnalysisSchema> findAllByName(String name, Pageable pageable);
  Integer countAllByName(String name);
  <T> List<T> findDistinctBy(Class<T> projection);

}
