package org.icgc.dcc.song.importer.download.fetcher;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.importer.model.DataContainer;
import org.icgc.dcc.song.importer.model.PortalDonorMetadata;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;

import java.util.ArrayList;

import static java.util.stream.Collectors.toSet;
import static org.icgc.dcc.song.importer.convert.DonorConverter.getDonorId;

@Slf4j
@RequiredArgsConstructor
public class DataFetcher {

  private final FileFetcher fileFetcher;
  private final DonorFetcher donorFetcher;

  @SneakyThrows
  public DataContainer fetchData() {
    val portalFileMetadataListCandidate = fileFetcher.fetchPortalFileMetadatas();
    val portalDonorMetadatas = donorFetcher.fetchPortalDonorMetadataSet(portalFileMetadataListCandidate);
    val goodDonorIdSet = portalDonorMetadatas
        .stream()
        .map(PortalDonorMetadata::getDonorId)
        .collect(toSet());

    // Remove files that map to errored donorIds
    int numRejectedFiles = 0;
    ArrayList<PortalFileMetadata> portalFileMetadatas = Lists.newArrayList();
    for(val portalFileMetadata : portalFileMetadataListCandidate){
      val donorId = getDonorId(portalFileMetadata);
      if (goodDonorIdSet.contains(donorId)){
        portalFileMetadatas.add(portalFileMetadata);
      } else {
        log.info("Skipped bad DonorId [{}] PortalFileMetadata[{}]: {}", donorId, ++numRejectedFiles, portalFileMetadata);
      }
    }
    return DataContainer.createDataContainer(portalDonorMetadatas, portalFileMetadatas);
  }

  public static DataFetcher createDataFetcher(FileFetcher fileFetcher,
      DonorFetcher donorFetcher) {
    return new DataFetcher(fileFetcher, donorFetcher);
  }

}
