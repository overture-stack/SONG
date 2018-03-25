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
import lombok.val;
import org.icgc.dcc.song.importer.model.PortalDonorMetadata;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.server.model.entity.Study;

import java.util.Set;

import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.icgc.dcc.song.importer.convert.Converters.NA;

@RequiredArgsConstructor
public class StudyConverter {
  private static final String DEFAULT_PROJECT_NAME = "";
  private static final String DEFAULT_STUDY_ORGANIZATION = "ICGC";

  public Set<Study> convertStudies(@NonNull Set<PortalDonorMetadata> portalDonorMetadataSet){
    return portalDonorMetadataSet.stream()
        .map(StudyConverter::convertToStudy)
        .collect(toImmutableSet());
  }

  public static Study convertToStudy(@NonNull PortalDonorMetadata portalDonorMetadata){
    val s=Study.create(
        getStudyId(portalDonorMetadata),
        getStudyName(portalDonorMetadata),
        getStudyOrganization(),
        getStudyDescription()
    );
    s.setInfo(getStudyInfo());
    return s;
  }

  public static String getStudyId(@NonNull PortalDonorMetadata portalDonorMetadata){
    return portalDonorMetadata.getProjectId();
  }

  public static String getStudyId(@NonNull PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getProjectCode();
  }

  public static String getStudyName(@NonNull PortalDonorMetadata portalDonorMetadata){
    return portalDonorMetadata.getProjectName().orElse(DEFAULT_PROJECT_NAME);
  }

  public static String getStudyDescription(){
    return NA;
  }

  public static String getStudyInfo(){
    return NA;
  }

  public static String getStudyOrganization(){
    return DEFAULT_STUDY_ORGANIZATION;
  }

  public static StudyConverter createStudyConverter() {
    return new StudyConverter();
  }

}
