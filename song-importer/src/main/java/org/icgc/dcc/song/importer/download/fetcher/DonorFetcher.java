package org.icgc.dcc.song.importer.download.fetcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.icgc.dcc.song.importer.download.PortalDonorIdFetcher;
import org.icgc.dcc.song.importer.model.PortalDonorMetadata;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.importer.parser.DonorPortalJsonParser;
import org.icgc.dcc.song.importer.parser.NormalSpecimenParser;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static lombok.Lombok.sneakyThrow;

@Slf4j
@RequiredArgsConstructor
public class DonorFetcher {

  private final PortalDonorIdFetcher portalDonorIdFetcher;


  public Set<PortalDonorMetadata> fetchPortalDonorMetadataSet(List<PortalFileMetadata> portalFileMetadataList){
    val donorSet = ImmutableSet.<PortalDonorMetadata>builder();
    val donorIdSet = Sets.<String>newHashSet();
    int numErrorDonorIds = 0;
    for (val portalFileMetadata : portalFileMetadataList){
      val donorId = portalFileMetadata.getDonorId();
      val fileId = portalFileMetadata.getFileId();
      if (!donorIdSet.contains(donorId)){ //Want to minimize redundant network traffic (fetching)
        try {
          val portalDonorMetadata = fetchPortalDonorMetadata(donorId);
          donorSet.add(portalDonorMetadata);
        } catch(Throwable t){
          log.error("DONOR_FETCH_ERROR[{}]: donorId [{}] data is malformed in FileId [{}]. Error recorded",
              ++numErrorDonorIds, donorId , fileId);
        }
        donorIdSet.add(donorId);
      }

    }
    return donorSet.build();
  }

  public PortalDonorMetadata fetchPortalDonorMetadata(String donorId){
    val donorMetadata = portalDonorIdFetcher.getDonorMetadata(donorId);
    return convertToPortalDonorMetadata(donorMetadata);
  }

  public static DonorFetcher createDonorFetcher(PortalDonorIdFetcher portalDonorIdFetcher){
    return new DonorFetcher(portalDonorIdFetcher);
  }

  public static PortalDonorMetadata convertToPortalDonorMetadata(JsonNode donor){
    try{
      val parser = NormalSpecimenParser.createNormalSpecimenParser(donor);
      return PortalDonorMetadata.builder()
          .donorId(DonorPortalJsonParser.getDonorId(donor))
          .projectId(DonorPortalJsonParser.getProjectId(donor))
          .projectName(DonorPortalJsonParser.getProjectName(donor))
          .submittedDonorId(DonorPortalJsonParser.getSubmittedDonorId(donor))
          .gender(DonorPortalJsonParser.getGender(donor))
          .normalAnalyzedId(parser.getNormalAnalyzedId())
          .normalSampleId(parser.getNormalSampleId())
          .normalSpecimenId(parser.getNormalSpecimenId())
          .normalSpecimenType(parser.getNormalSpecimenType())
          .normalSubmittedSpecimenId(parser.getNormalSubmittedSpecimenId())
          .build();
    } catch(Throwable t){
      log.info("Error: {}\nOBJECT_DATA_DUMP:\n{}", t.getMessage(), JsonUtils.toPrettyJson(donor));
      throw sneakyThrow(t);
    }
  }

  @Value
  public static class DonorFetcherStatus {

    private final HashMap<String, PortalDonorMetadata> map;
    private final Set<String> erroredDonorIds;

    public Set<String> getErroredDonorIds(){
      return ImmutableSet.copyOf(erroredDonorIds);
    }

    public boolean hasErroredDonorIds(){
      return !erroredDonorIds.isEmpty();
    }


    public static DonorFetcherStatus createDataFetcherStatus(
        HashMap<String, PortalDonorMetadata> map, Set<String> erroredDonorIds) {
      return new DonorFetcherStatus(map, erroredDonorIds);
    }

  }

}
