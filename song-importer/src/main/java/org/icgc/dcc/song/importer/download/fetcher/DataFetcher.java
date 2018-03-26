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

package org.icgc.dcc.song.importer.download.fetcher;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.common.core.util.Joiners;
import org.icgc.dcc.song.importer.download.DownloadIterator;
import org.icgc.dcc.song.importer.filters.Filter;
import org.icgc.dcc.song.importer.filters.impl.DataBundleFileFilter;
import org.icgc.dcc.song.importer.model.DataContainer;
import org.icgc.dcc.song.importer.model.DccMetadata;
import org.icgc.dcc.song.importer.model.PortalDonorMetadata;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.importer.storage.SimpleDccStorageClient;
import org.icgc.dcc.song.server.model.entity.File;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.icgc.dcc.song.importer.dao.dcc.impl.DccMetadataMemoryDao.createDccMetadataMemoryDao;
import static org.icgc.dcc.song.importer.download.fetcher.DccMetadataFetcher.createDccMetadataFetcher;
import static org.icgc.dcc.song.importer.model.DataContainer.createDataContainer;

@Slf4j
@RequiredArgsConstructor
public class DataFetcher {


  @NonNull private final DonorFetcher donorFetcher;
  @NonNull private final Filter<PortalFileMetadata> portalFileMetadataFilter;
  @NonNull private final DownloadIterator<DccMetadata> dccMetadataDownloadIterator;
  @NonNull private final SimpleDccStorageClient simpleDccStorageClient;
  @NonNull private final DownloadIterator<PortalFileMetadata> portalFileMetadataDownloadIterator;
  @NonNull private final DataBundleFileFilter dataBundleFileFilter;

  @SneakyThrows
  public DataContainer fetchData() {

    val portalFileMetadataListCandidate = fetchPortalFileMetadata();

    // Will only return properly parsed PortalDonorMetadatas (since there might be potato donors)
    val portalDonorMetadatas = fetchPortalDonorMetadata(portalFileMetadataListCandidate);

    // Create a list of good donorIds
    val goodDonorIds = getGoodDonorIdsOnly(portalDonorMetadatas);

    // Filter portalFileMetadatas by ignoring files belonging to potato donors (errored donorIds), and files matching
    // the portalFileMetadataFilter criteria
    val portalFileMetadatas = filterPortalFileMetadata(portalFileMetadataListCandidate, goodDonorIds);

    // Calculate and log some stats
    val numFilesFiltered = portalFileMetadataListCandidate.size() - portalFileMetadatas.size();
    log.info("Filtered out {} files from input portalFileMetadataList with {} files",
        numFilesFiltered, portalFileMetadataListCandidate.size());

    // Fetch dccMetadata for the final portalFileMetadata list
    val dccMetadataFiles = fetchDccMetadataFiles(portalFileMetadatas);

    return createDataContainer(portalDonorMetadatas, portalFileMetadatas, dccMetadataFiles);
  }

  private Set<File> fetchDccMetadataFiles(List<PortalFileMetadata> portalFileMetadatas){
    val allDccMetadatas = dccMetadataDownloadIterator.stream()
        .collect(toImmutableSet());
    val dccMetadataDao = createDccMetadataMemoryDao(allDccMetadatas);
    val dccMetadataFetcher = createDccMetadataFetcher(dccMetadataDao, simpleDccStorageClient);
    return dccMetadataFetcher.fetchDccMetadataFiles(portalFileMetadatas);
  }

  private Set<String> getGoodDonorIdsOnly(Set<PortalDonorMetadata> portalDonorMetadatas){
    return portalDonorMetadatas.stream()
        .map(PortalDonorMetadata::getDonorId)
        .collect(toSet());
  }

  private List<PortalFileMetadata> filterPortalFileMetadata(List<PortalFileMetadata> portalFileMetadataListCandidate,
      Set<String> goodDonorIds){
    val result = portalFileMetadataListCandidate.stream()
        .filter(x -> goodDonorIds.contains(x.getDonorId()))
        .filter(portalFileMetadataFilter::isPass)
        .collect(toList());
    val unProcessedIds = dataBundleFileFilter
        .getDataBundleIdFilter()
        .getUnProcessedIds();
    log.warn("[DATA_BUNDLE_FILE_FILTER] The following {} dataBundleIds were not processed: "
            + "\n{}",
        unProcessedIds.size(), Joiners.NEWLINE.join(unProcessedIds));
    return result;
  }

  private Set<PortalDonorMetadata> fetchPortalDonorMetadata(List<PortalFileMetadata> portalFileMetadataListCandidate){
    return donorFetcher.fetchPortalDonorMetadataSet(portalFileMetadataListCandidate);
  }

  private List<PortalFileMetadata> fetchPortalFileMetadata(){
    return portalFileMetadataDownloadIterator.stream()
        .collect(toImmutableList());
  }

  public static DataFetcher createDataFetcher( DonorFetcher donorFetcher,
      Filter<PortalFileMetadata> portalFileMetadataFilter,
      DownloadIterator<DccMetadata> dccMetadataDownloadIterator,
      SimpleDccStorageClient simpleDccStorageClient,
      DownloadIterator<PortalFileMetadata> portalFileMetadataDownloadIterator,
      DataBundleFileFilter dataBundleFileFilter ) {
    return new DataFetcher(donorFetcher, portalFileMetadataFilter, dccMetadataDownloadIterator,
        simpleDccStorageClient, portalFileMetadataDownloadIterator, dataBundleFileFilter);
  }
}
