package org.icgc.dcc.song.server.importer.model;

import com.google.common.collect.ImmutableSet;
import lombok.NonNull;
import lombok.Value;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;

@Value
public class DataContainer implements Serializable {

  @NonNull private final ArrayList<PortalDonorMetadata> portalDonorMetadataList;
  @NonNull private final ArrayList<PortalFileMetadata> portalFileMetadataList;

  public static DataContainer createDataContainer(
      Set<PortalDonorMetadata> portalDonorMetadataSet,
      List<PortalFileMetadata> portalFileMetadataList) {
    return new DataContainer(newArrayList(portalDonorMetadataSet), newArrayList(portalFileMetadataList));
  }

  public Set<PortalDonorMetadata> getPortalDonorMetadataSet(){
    return ImmutableSet.copyOf(portalDonorMetadataList);
  }

  public Set<PortalFileMetadata> getPortalFileMetadataSet(){
    return ImmutableSet.copyOf(portalFileMetadataList);
  }

}
