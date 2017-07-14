package org.icgc.dcc.song.server.importer.convert;

import lombok.RequiredArgsConstructor;
import org.icgc.dcc.song.server.importer.model.PortalDonorMetadata;
import org.icgc.dcc.song.server.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.server.model.entity.Study;

import java.util.Set;

import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.icgc.dcc.song.server.importer.convert.Converters.NA;

@RequiredArgsConstructor
public class StudyConverter {
  private static final String DEFAULT_PROJECT_NAME = "";
  private static final String DEFAULT_STUDY_ORGANIZATION = "ICGC";

  public Set<Study> convertStudies(Set<PortalDonorMetadata> portalDonorMetadataSet){
    return portalDonorMetadataSet.stream()
        .map(StudyConverter::convertToStudy)
        .collect(toImmutableSet());
  }

  public static Study convertToStudy(PortalDonorMetadata portalDonorMetadata){
    return Study.create(
        getStudyId(portalDonorMetadata),
        getStudyName(portalDonorMetadata),
        getStudyOrganization(),
        getStudyDescription(),
        getStudyInfo()
    );
  }

  public static String getStudyId(PortalDonorMetadata portalDonorMetadata){
    return portalDonorMetadata.getProjectId();
  }

  public static String getStudyId(PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getProjectCode();
  }

  public static String getStudyName(PortalDonorMetadata portalDonorMetadata){
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
