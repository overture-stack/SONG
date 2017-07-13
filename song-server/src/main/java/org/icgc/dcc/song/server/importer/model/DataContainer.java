package org.icgc.dcc.song.server.importer.model;

import lombok.Value;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@Value
public class DataContainer implements Serializable {

  private final ArrayList<PortalDonorMetadata> portalDonorMetadataList;
  private final ArrayList<PortalFileMetadata> portalFileMetadataList;

  public static DataContainer createDataContainer(
      List<PortalDonorMetadata> portalDonorMetadataList,
      List<PortalFileMetadata> portalFileMetadataList) {
    return new DataContainer(newArrayList(portalDonorMetadataList), newArrayList(portalFileMetadataList));
  }

}
