package org.icgc.dcc.song.server.repository;

import org.icgc.dcc.song.server.model.legacy.LegacyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LegacyEntityRepository extends JpaRepository<LegacyEntity, String>{

}
