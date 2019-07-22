package bio.overture.song.server.repository;

import bio.overture.song.server.model.entity.AnalysisType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnalysisTypeRepository extends JpaRepository<AnalysisType, Integer>{

  Optional<AnalysisType> findFirstByNameOrderByIdDesc(String name);
  List<AnalysisType> findAllByNameOrderByIdDesc(String name);
  Page<AnalysisType> findAllByName(String name, Pageable pageable);
  Integer countAllByName(String name);
  <T> List<T> findBy(Class<T> projection);
  <T> List<T> findDistinctBy(Class<T> projection);

}
