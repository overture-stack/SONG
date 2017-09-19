package org.icgc.dcc.song.importer.download.fetcher;

import lombok.NonNull;
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

  @NonNull private final String repoName;
  @NonNull private final FileFetcher fileFetcher;
  @NonNull private final DonorFetcher donorFetcher;

  @SneakyThrows
  public DataContainer fetchData() {
    val portalFileMetadataListCandidate = fileFetcher.fetchPortalFileMetadatas(repoName);
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

  public static DataFetcher createDataFetcher(String repoName,
      FileFetcher fileFetcher, DonorFetcher donorFetcher) {
    return new DataFetcher(repoName, fileFetcher, donorFetcher);
  }

}
