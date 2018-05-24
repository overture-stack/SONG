package org.icgc.dcc.song.server.repository;

import org.icgc.dcc.song.server.model.LegacyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LegacyEntityRepository extends JpaRepository<LegacyEntity, String>{

}
