package org.icgc.dcc.song.server.importer.persistence;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.icgc.dcc.song.server.importer.download.fetcher.DonorFetcher;
import org.icgc.dcc.song.server.importer.model.PortalDonorMetadata;
import org.icgc.dcc.song.server.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.server.importer.persistence.filerestorer.FileRestorer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static com.google.common.collect.Lists.newArrayList;
import static org.icgc.dcc.song.server.importer.persistence.PersistenceFactory.createPersistenceFactory;

@RequiredArgsConstructor
public class DonorPersistenceFactory implements Callable<List<PortalDonorMetadata>> {

  @NonNull private final String name;
  @NonNull private final PersistenceFactory<ArrayList<PortalDonorMetadata>, String> persistenceFactory;

  @Override
  public List<PortalDonorMetadata> call() throws Exception {
    return getPortalDonorMetadatas();
  }

  public List<PortalDonorMetadata> getPortalDonorMetadatas(){
    return persistenceFactory.getObject(name);
  }

  public static DonorPersistenceFactory createDonorPersistenceFactory(
      String name,
      @NonNull DonorFetcher donorFetcher,
      @NonNull FileRestorer<ArrayList<PortalDonorMetadata>, String> fileRestorer,
      @NonNull List<PortalFileMetadata> portalFileMetadataList,
      PersistenceFactory<ArrayList<PortalDonorMetadata>, String> persistenceFactory){
    val factory = createPersistenceFactory(fileRestorer,
        () -> executeDonorFetching(portalFileMetadataList, donorFetcher));
    return createDonorPersistenceFactory(name, factory);
  }

  private static ArrayList<PortalDonorMetadata>  executeDonorFetching(List<PortalFileMetadata> portalFileMetadataList,DonorFetcher donorFetcher){
    return newArrayList(donorFetcher.fetchPortalDonorMetadataList(portalFileMetadataList));
  }

  public static DonorPersistenceFactory createDonorPersistenceFactory(
      String name,
      PersistenceFactory<ArrayList<PortalDonorMetadata>, String> persistenceFactory){
    return new DonorPersistenceFactory(name, persistenceFactory);
  }

}
