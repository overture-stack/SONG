package org.icgc.dcc.song.server.importer.download.fetcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableSet;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.icgc.dcc.song.server.importer.download.PortalDonorIdFetcher;
import org.icgc.dcc.song.server.importer.model.PortalDonorMetadata;
import org.icgc.dcc.song.server.importer.model.PortalFileMetadata;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static lombok.Lombok.sneakyThrow;
import static org.icgc.dcc.song.server.importer.parser.DonorPortalJsonParser.getDonorId;
import static org.icgc.dcc.song.server.importer.parser.DonorPortalJsonParser.getGender;
import static org.icgc.dcc.song.server.importer.parser.DonorPortalJsonParser.getProjectId;
import static org.icgc.dcc.song.server.importer.parser.DonorPortalJsonParser.getProjectName;
import static org.icgc.dcc.song.server.importer.parser.DonorPortalJsonParser.getSubmittedDonorId;
import static org.icgc.dcc.song.server.importer.parser.NormalSpecimenParser.createNormalSpecimenParser;

@Slf4j
@RequiredArgsConstructor
public class DonorFetcher {

  private final PortalDonorIdFetcher portalDonorIdFetcher;


  public Set<PortalDonorMetadata> fetchPortalDonorMetadataSet(List<PortalFileMetadata> portalFileMetadataList){
    val donorIds = portalFileMetadataList.stream()
        .map(PortalFileMetadata::getDonorId)
        .collect(toSet());
    val set = ImmutableSet.<PortalDonorMetadata>builder();
    int numErrorDonorIds = 0;
    for (val donorId : donorIds){
      try {
        val portalDonorMetadata = fetchPortalDonorMetadata(donorId);
        set.add(portalDonorMetadata);
      } catch(Throwable t){
        log.error("DONOR_FETCH_ERROR[{}]: donorId [{}] data is malformed. Error recorded",
            ++numErrorDonorIds, donorId );
      }
    }
    return set.build();
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
      val parser = createNormalSpecimenParser(donor);
      return PortalDonorMetadata.builder()
          .donorId(getDonorId(donor))
          .projectId(getProjectId(donor))
          .projectName(getProjectName(donor))
          .submittedDonorId(getSubmittedDonorId(donor))
          .gender(getGender(donor))
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
