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

package org.icgc.dcc.song.importer.dao;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.icgc.dcc.song.importer.convert.DonorConverter;
import org.icgc.dcc.song.importer.model.PortalDonorMetadata;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static org.icgc.dcc.song.importer.convert.DonorConverter.getDonorId;

@RequiredArgsConstructor
public class DonorDao  {

  @NonNull private final Map<String,PortalDonorMetadata> portalDonorMetadataMap;

  public PortalDonorMetadata getPortalDonorMetadata(String donorId){
    checkArgument(portalDonorMetadataMap.containsKey(donorId),
        "The donorId [%s] DNE", donorId);
    return portalDonorMetadataMap.get(donorId);
  }

  public static DonorDao createDonorDao(
      Map<String, PortalDonorMetadata> portalDonorMetadataMap) {
    return new DonorDao(portalDonorMetadataMap);
  }

  public static DonorDao createDonorDao(Iterable<PortalDonorMetadata> portalDonorMetadataIterable){
    val map = Maps.<String, PortalDonorMetadata>newHashMap();
    for (val portalDonorMetadata : portalDonorMetadataIterable){
      val donorId = DonorConverter.getDonorId(portalDonorMetadata);
      checkArgument(!map.containsKey(donorId), "Map cannot have duplicates for donorId [%s]", donorId);
      map.put(donorId, portalDonorMetadata);
    }
    return createDonorDao(ImmutableMap.copyOf(map));
  }

}
