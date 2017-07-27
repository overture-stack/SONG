package org.icgc.dcc.song.importer.convert;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.icgc.dcc.song.importer.model.PortalDonorMetadata;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.server.model.entity.Donor;

import java.util.Set;

import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.icgc.dcc.song.importer.convert.Converters.NA;

@RequiredArgsConstructor
public class DonorConverter {

  private static final String DONOR_GENDER_DEFAULT = "unspecified";

  public Set<Donor> convertDonors(@NonNull Set<PortalDonorMetadata> portalDonorMetadataSet){
    return portalDonorMetadataSet.stream()
        .map(DonorConverter::convertToDonor)
        .collect(toImmutableSet());
  }

  public static Donor convertToDonor(@NonNull PortalDonorMetadata portalDonorMetadata){
    return Donor.create(
        getDonorId(portalDonorMetadata),
        getDonorSubmitterId(portalDonorMetadata),
        StudyConverter.getStudyId(portalDonorMetadata),
        getGender(portalDonorMetadata));
  }

  public static String getDonorId(@NonNull PortalDonorMetadata portalDonorMetadata){
    return portalDonorMetadata.getDonorId();
  }

  public static String getDonorSubmitterId(@NonNull PortalDonorMetadata portalDonorMetadata){
    return portalDonorMetadata.getSubmittedDonorId();
  }

  public static String getGender(@NonNull PortalDonorMetadata portalDonorMetadata){
    return portalDonorMetadata.getGender().orElse(DONOR_GENDER_DEFAULT);
  }

  public static String getDonorId(@NonNull PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getDonorId();
  }

  public static DonorConverter createDonorConverter() {
    return new DonorConverter();
  }

}
