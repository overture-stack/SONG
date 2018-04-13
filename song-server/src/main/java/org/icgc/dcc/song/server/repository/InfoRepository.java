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
import org.icgc.dcc.song.server.model.entity.Info;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.isNull;
import static org.icgc.dcc.song.core.utils.JsonUtils.toJson;
import static org.icgc.dcc.song.core.utils.JsonUtils.toJsonNode;

public interface InfoRepository extends JpaRepository<Info, String>{

  // RTISMA_TODO:  eliminate repo complexity and just use save method
  default int create(String id, String idType, String jsonInfo){
    val s = save(Info.createInfo(id, idType, jsonInfo));
    return 1;
  }

  // RTISMA_TODO: fix this, its not really used. Shhould stick to save method instead
  default int set(String id, String idType, String jsonInfo){
    val s = save(Info.createInfo(id, idType, jsonInfo));
    return 1;
  }

  // RTISMA_TODO: this should be done in the service layer.
  default String readInfo(String id, String id_type){
    val req = Info.createInfo(id, id_type, (String)null);
    val results = findAll(Example.of(req));
    checkArgument(results.size() <= 1, "cannot have more than one");
    if (results.isEmpty()){
      return null;
    } else {
      val value = results.get(0).getInfo();
      if (isNull(value) || value.isEmpty()){
        return null;
      }
      return toJson(toJsonNode(value));
    }
  }

  // RTISMA_TODO: remove this and just use the default jpa is Exists
  default String readType(String id, String id_type) {
    val req = Info.createInfo(id, id_type, (String)null);
    val results = findAll(Example.of(req));
    checkArgument(results.size() <= 1, "cannot have more than one");
    return results.isEmpty() ? null : results.get(0).getIdType();
  }

  // RTISMA_TODO: fix this, its halfassed
  default int delete(String id, String id_type){
    val req = Info.createInfo(id, id_type, (String)null);
    this.delete(req);
    return 1;
  }

}
