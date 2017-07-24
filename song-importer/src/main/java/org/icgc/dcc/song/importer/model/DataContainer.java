package org.icgc.dcc.song.importer.model;

import com.google.common.collect.ImmutableSet;
import lombok.NonNull;
import lombok.Value;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;

@Value
public class DataContainer implements Serializable {

  @NonNull private final ArrayList<PortalDonorMetadata> portalDonorMetadataList;
  @NonNull private final ArrayList<PortalFileMetadata> portalFileMetadataList;

  public static DataContainer createDataContainer(
      Iterable<PortalDonorMetadata> portalDonorMetadataList,
      Iterable<PortalFileMetadata> portalFileMetadataList) {
    return new DataContainer(newArrayList(portalDonorMetadataList), newArrayList(portalFileMetadataList));
  }

  public Set<PortalDonorMetadata> getPortalDonorMetadataSet(){
    return ImmutableSet.copyOf(portalDonorMetadataList);
  }

  public Set<PortalFileMetadata> getPortalFileMetadataSet(){
    return ImmutableSet.copyOf(portalFileMetadataList);
  }

}
