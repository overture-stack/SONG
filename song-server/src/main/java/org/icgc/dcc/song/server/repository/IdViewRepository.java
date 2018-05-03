package org.icgc.dcc.song.server.repository;

import org.icgc.dcc.song.server.model.entity.IdView;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdViewRepository extends JpaRepository<IdView, String> {

}
