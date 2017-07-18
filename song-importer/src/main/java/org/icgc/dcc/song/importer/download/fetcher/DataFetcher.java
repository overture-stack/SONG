package org.icgc.dcc.song.importer.download.fetcher;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.importer.model.DataContainer;
import org.icgc.dcc.song.importer.model.PortalDonorMetadata;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.icgc.dcc.song.importer.model.DataContainer.createDataContainer;

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

    val portalFileMetadatas = portalFileMetadataListCandidate.stream()
        .filter(x -> goodDonorIdSet.contains(x.getDonorId()))
        .collect(toList());

    // reject files that map to errored donorIds
    return createDataContainer(portalDonorMetadatas, portalFileMetadatas);
  }

  public static DataFetcher createDataFetcher(FileFetcher fileFetcher,
      DonorFetcher donorFetcher) {
    return new DataFetcher(fileFetcher, donorFetcher);
  }

}
