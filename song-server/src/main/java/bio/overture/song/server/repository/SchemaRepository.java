package bio.overture.song.server.repository;

import bio.overture.song.server.model.ExperimentSchema;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchemaRepository extends JpaRepository<ExperimentSchema, String> {


}
