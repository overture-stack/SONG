package org.icgc.dcc.song.server.importer.convert;

import lombok.RequiredArgsConstructor;
import org.icgc.dcc.song.server.importer.model.PortalDonorMetadata;
import org.icgc.dcc.song.server.model.entity.Donor;

import java.util.Set;

import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.icgc.dcc.song.server.importer.convert.Converters.NA;

@RequiredArgsConstructor
public class DonorConverter {

  private static final String DONOR_GENDER_DEFAULT = "unspecified";

  public Set<Donor> convertDonors(Set<PortalDonorMetadata> portalDonorMetadataSet){
    return portalDonorMetadataSet.stream()
        .map(DonorConverter::convertToDonor)
        .collect(toImmutableSet());
  }

  public static Donor convertToDonor(PortalDonorMetadata portalDonorMetadata){
    return Donor.create(
        getDonorId(portalDonorMetadata),
        getDonorSubmitterId(portalDonorMetadata),
        getStudyId(portalDonorMetadata),
        getGender(portalDonorMetadata),
        NA);
  }

  public static String getDonorId(PortalDonorMetadata portalDonorMetadata){
    return portalDonorMetadata.getDonorId();
  }

  public static String getDonorSubmitterId(PortalDonorMetadata portalDonorMetadata){
    return portalDonorMetadata.getSubmittedDonorId();
  }

  public static String getStudyId(PortalDonorMetadata portalDonorMetadata){
    return portalDonorMetadata.getProjectId();
  }

  public static String getGender(PortalDonorMetadata portalDonorMetadata){
    return portalDonorMetadata.getGender().orElse(DONOR_GENDER_DEFAULT);
  }

  public static DonorConverter createDonorConverter() {
    return new DonorConverter();
  }
}
