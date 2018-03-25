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

package org.icgc.dcc.song.importer.model;

import com.google.common.collect.ImmutableSet;
import lombok.NonNull;
import lombok.Value;
import org.icgc.dcc.song.server.model.entity.File;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;

@Value
public class DataContainer implements Serializable {

  @NonNull private final ArrayList<PortalDonorMetadata> portalDonorMetadataList;
  @NonNull private final ArrayList<PortalFileMetadata> portalFileMetadataList;
  @NonNull private final ArrayList<File> dccMetadataFiles;

  public static DataContainer createDataContainer(
      Iterable<PortalDonorMetadata> portalDonorMetadataList,
      Iterable<PortalFileMetadata> portalFileMetadataList,
      Iterable<File> dccMetadataFiles ) {
    return new DataContainer(newArrayList(portalDonorMetadataList),
        newArrayList(portalFileMetadataList),
        newArrayList(dccMetadataFiles) );
  }

  public Set<PortalDonorMetadata> getPortalDonorMetadataSet(){
    return ImmutableSet.copyOf(portalDonorMetadataList);
  }

  public Set<PortalFileMetadata> getPortalFileMetadataSet(){
    return ImmutableSet.copyOf(portalFileMetadataList);
  }

}
