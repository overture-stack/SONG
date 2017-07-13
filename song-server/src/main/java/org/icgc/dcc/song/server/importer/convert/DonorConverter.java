package org.icgc.dcc.song.server.importer.convert;

import lombok.RequiredArgsConstructor;
import org.icgc.dcc.song.server.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.server.model.entity.Donor;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class DonorConverter {

  private final List<PortalFileMetadata> portalFileMetadataList;

  public Set<Donor> convertDonors(){
    return null;


  }

//  public static Donor convertToDonor(PortalFileMetadata portalFileMetadata){
//    Donor.create(
//        portalFileMetadata.getDonorId(),
//        portalFileMetadata.getSubmittedDonorId(),
//        portalFileMetadata.getProjectCode(),
//        portalDonorMetadata.getGender().orElse(DONOR_GENDER_DEFAULT),
//        NA);
//    return Donor.create(
//        getDonorId()
//
//    );
//
//  }

  public static String getDonorId(PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getDonorId();
  }



}
