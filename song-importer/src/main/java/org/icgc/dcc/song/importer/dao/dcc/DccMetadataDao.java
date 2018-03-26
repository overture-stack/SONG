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

package org.icgc.dcc.song.importer.dao.dcc;

import org.icgc.dcc.song.importer.model.DccMetadata;

import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

public interface DccMetadataDao {

  Optional<DccMetadata> findByObjectId(String objectId);

  Set<DccMetadata> findByMultiObjectIds(Set<String> objectIds);

  default Set<DccMetadata> findByMultiObjectIds(String ... objectIds){
    return findByMultiObjectIds(newHashSet(objectIds));
  }

}
