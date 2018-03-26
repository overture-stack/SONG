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

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface InfoRepository {

  @SqlUpdate("INSERT INTO Info (id, id_type, info) VALUES(:id,:type,:info)")
  int create(@Bind("id") String id, @Bind("type") String id_type, @Bind("info") String jsonInfo);

  @SqlUpdate("UPDATE Info set info=:info where id=:id AND id_type=:type")
  int set(@Bind("id") String id, @Bind("type") String id_type, @Bind("info") String jsonInfo);

  @SqlQuery("SELECT info from Info where id_type=:type AND id=:id")
  String readInfo(@Bind("id") String id, @Bind("type") String id_type);

  @SqlQuery("SELECT id_type from Info where id_type=:type AND id=:id")
  String readType(@Bind("id") String id, @Bind("type") String id_type);

  @SqlUpdate("DELETE from Info where id=:id AND id_type=:type")
  int delete(@Bind("id") String id, @Bind("type") String id_type);

}
