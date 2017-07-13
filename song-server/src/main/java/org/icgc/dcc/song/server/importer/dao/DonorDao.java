package org.icgc.dcc.song.server.importer.dao;

import lombok.RequiredArgsConstructor;
import org.icgc.dcc.song.server.importer.model.PortalDonorMetadata;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

@RequiredArgsConstructor
public class DonorDao  {

  private final Map<String,PortalDonorMetadata> portalDonorMetadataMap;

  public PortalDonorMetadata getPortalDonorMetadata(String donorId){
    checkArgument(portalDonorMetadataMap.containsKey(donorId),
        "The donorId [%s] DNE", donorId);
    return portalDonorMetadataMap.get(donorId);
  }

  public static DonorDao createDonorDao(
      Map<String, PortalDonorMetadata> portalDonorMetadataMap) {
    return new DonorDao(portalDonorMetadataMap);
  }

}
