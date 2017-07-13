package org.icgc.dcc.song.server.importer.convert;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.icgc.dcc.song.server.importer.dao.DonorDao;
import org.icgc.dcc.song.server.importer.model.PortalDonorMetadata;
import org.icgc.dcc.song.server.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.server.model.entity.Study;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.groupingBy;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.icgc.dcc.song.server.importer.convert.Converters.NA;

@RequiredArgsConstructor
public class StudyConverter {
  private static final String DEFAULT_PROJECT_NAME = "";
  private static final String DEFAULT_STUDY_ORGANIZATION = "ICGC";

  private final List<PortalFileMetadata> portalFileMetadatas;
  private final Map<String, String> studyIdNameMap;

  private StudyConverter(List<PortalFileMetadata> portalFileMetadatas,
      DonorDao donorDao) {
    this.portalFileMetadatas = portalFileMetadatas;
    this.studyIdNameMap = mapStudyIdWithStudyName(portalFileMetadatas, donorDao);
  }

  public Set<Study> convertStudies(){
    return portalFileMetadatas.stream()
        .map(this::convertToStudy)
        .collect(toImmutableSet());
  }

  private Study convertToStudy(PortalFileMetadata portalFileMetadata){
    return Study.create(
        getStudyId(portalFileMetadata),
        getStudyName(portalFileMetadata),
        getStudyOrganization(),
        getStudyDescription(),
        getStudyInfo()
    );
  }

  private String getStudyName(PortalFileMetadata portalFileMetadata){
    val studyId = getStudyId(portalFileMetadata);
    checkState(studyIdNameMap.containsKey(studyId),
        "The studyId [%s] does not exist for the PortalFileMetadata [%s]",
        studyId, portalFileMetadata);
    return studyIdNameMap.get(studyId);
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


  public static String getStudyId(PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getProjectCode();
  }


  public static String getStudyName(PortalDonorMetadata portalDonorMetadata){
    return portalDonorMetadata.getProjectName().orElse(DEFAULT_PROJECT_NAME);
  }

  public static StudyConverter createStudyConverter(List<PortalFileMetadata> portalFileMetadatas,
      DonorDao donorDao) {
    return new StudyConverter(portalFileMetadatas, donorDao);
  }

  private static Map<String, List<PortalFileMetadata>> groupByStudyId(List<PortalFileMetadata> portalFileMetadataList){
    return portalFileMetadataList.stream()
        .collect(groupingBy(StudyConverter::getStudyId));
  }

  private static Map<String, String > mapStudyIdWithStudyName(List<PortalFileMetadata> portalFileMetadataList, DonorDao donorDao){
    val studyIdGrouping = groupByStudyId(portalFileMetadataList);
    val studyIdDonorMap = ImmutableMap.<String,String>builder();
    for (val entry : studyIdGrouping.entrySet()){
      val studyId = entry.getKey();
      val donorId = entry.getValue().get(0).getDonorId();
      val portalDonorMetadata = donorDao.getPortalDonorMetadata(donorId);
      studyIdDonorMap.put(studyId, getStudyName(portalDonorMetadata) );
    }
    return studyIdDonorMap.build();
  }


}
