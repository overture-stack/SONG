package bio.overture.song.server.repository;

import bio.overture.song.server.model.entity.AnalysisSchema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnalysisSchemaRepository extends JpaRepository<AnalysisSchema, Integer>{

  Optional<AnalysisSchema> findFirstByNameOrderByIdDesc(String name);
  List<AnalysisSchema> findAllByNameOrderByIdDesc(String name);
  Page<AnalysisSchema> findAllByName(String name, Pageable pageable);
  Integer countAllByName(String name);
  <T> List<T> findBy(Class<T> projection);
  <T> List<T> findDistinctBy(Class<T> projection);

}
