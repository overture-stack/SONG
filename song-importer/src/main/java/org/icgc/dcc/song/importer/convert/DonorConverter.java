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
