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

package org.icgc.dcc.song.importer.download.queries;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.icgc.dcc.common.core.json.JsonNodeBuilders;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.icgc.dcc.common.core.json.JsonNodeBuilders.array;

public interface PortalQuery {

  ObjectNode buildQuery();

  static ObjectNode createIs(String... values){
    return createIs(newArrayList(values));
  }

  static ObjectNode createIs(List<String> list){
    return JsonNodeBuilders.object()
        .with("is",
            array()
                .with(list)
                .end())
        .end();
  }
}
