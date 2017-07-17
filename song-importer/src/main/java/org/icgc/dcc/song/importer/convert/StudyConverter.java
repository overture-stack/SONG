package org.icgc.dcc.song.importer.convert;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.icgc.dcc.song.importer.model.PortalDonorMetadata;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.server.model.entity.Study;

import java.util.Set;

import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;

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
    return Study.create(
        getStudyId(portalDonorMetadata),
        getStudyName(portalDonorMetadata),
        getStudyOrganization(),
        getStudyDescription(),
        getStudyInfo()
    );
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
    return Converters.NA;
  }

  public static String getStudyInfo(){
    return Converters.NA;
  }

  public static String getStudyOrganization(){
    return DEFAULT_STUDY_ORGANIZATION;
  }

  public static StudyConverter createStudyConverter() {
    return new StudyConverter();
  }

}
