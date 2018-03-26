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

package org.icgc.dcc.song.importer.dao.dcc.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.icgc.dcc.song.importer.dao.dcc.DccMetadataDao;
import org.icgc.dcc.song.importer.model.DccMetadata;

import java.util.Optional;
import java.util.Set;

import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;

@RequiredArgsConstructor
public class DccMetadataMemoryDao implements DccMetadataDao {

  @NonNull private final Set<DccMetadata> data;

  @Override
  public Optional<DccMetadata> findByObjectId(String objectId) {
    return data.stream()
        .filter(x -> x.getId().equals(objectId))
        .findFirst();
  }

  @Override
  public Set<DccMetadata> findByMultiObjectIds(Set<String> objectIds) {
    return data.stream()
        .filter(x -> objectIds.contains(x.getId()))
        .collect(toImmutableSet());
  }

  public static DccMetadataMemoryDao createDccMetadataMemoryDao(Set<DccMetadata> data) {
    return new DccMetadataMemoryDao(data);
  }

}
