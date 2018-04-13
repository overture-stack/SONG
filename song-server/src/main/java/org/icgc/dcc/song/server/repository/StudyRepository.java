/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.icgc.dcc.song.server.repository;

import lombok.val;
import org.icgc.dcc.song.server.model.entity.Study;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;

public interface StudyRepository extends JpaRepository<Study, String>{

  default int create( String id,  String name,  String organization, String description){
    val s = Study.create(id, name, organization, description);
    this.save(s);

    return 1;
  }

  default Study read(String id){
    return findById(id).orElse(null);
  }

  default List<String> findAllStudies(){
    return this.findAll().stream()
        .map(Study::getStudyId)
        .collect(toImmutableList());
  }
}
