package org.icgc.dcc.song.server.importer.dao;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.icgc.dcc.song.server.importer.download.PortalDonorIdFetcher;
import org.icgc.dcc.song.server.importer.model.PortalDonorMetadata;

import static org.icgc.dcc.song.server.importer.convert.Converters.convertToPortalDonorMetadata;

@RequiredArgsConstructor
public class DonorDao {

  private final PortalDonorIdFetcher portalDonorIdFetcher;

  public PortalDonorMetadata getPortalDonorMetadata(String donorId){
    val donorMetadata = portalDonorIdFetcher.getDonorMetadata(donorId);
    return convertToPortalDonorMetadata(donorMetadata);
  }

  public static DonorDao createDonorDao(PortalDonorIdFetcher portalDonorIdFetcher) {
    return new DonorDao(portalDonorIdFetcher);
  }

}
