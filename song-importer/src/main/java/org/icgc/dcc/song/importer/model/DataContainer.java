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
