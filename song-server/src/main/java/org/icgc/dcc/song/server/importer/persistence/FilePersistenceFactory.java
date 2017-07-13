package org.icgc.dcc.song.server.importer.persistence;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.icgc.dcc.song.server.importer.download.fetcher.FileFetcher;
import org.icgc.dcc.song.server.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.server.importer.persistence.filerestorer.FileRestorer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static org.icgc.dcc.song.server.importer.persistence.PersistenceFactory.createPersistenceFactory;

@RequiredArgsConstructor
public class FilePersistenceFactory implements Callable<List<PortalFileMetadata>> {

  @NonNull private final String name;
  @NonNull private final PersistenceFactory<ArrayList<PortalFileMetadata>, String> persistenceFactory;

  @Override
  public List<PortalFileMetadata> call() throws Exception {
    return getPortalFileMetadatas();
  }

  public List<PortalFileMetadata> getPortalFileMetadatas(){
    return persistenceFactory.getObject(name);
  }

  public static FilePersistenceFactory createFilePersistenceFactory(
      String name,
      @NonNull FileRestorer<ArrayList<PortalFileMetadata>, String>  fileRestorer,
      @NonNull FileFetcher fileFetcher){
    val factory = createPersistenceFactory(fileRestorer,
        () -> (ArrayList<PortalFileMetadata>)fileFetcher.fetchPortalFileMetadatas());
    return createFilePersistenceFactory(name,factory);
  }

  public static FilePersistenceFactory createFilePersistenceFactory(String name,
      PersistenceFactory<ArrayList<PortalFileMetadata>, String> persistenceFactory) {
    return new FilePersistenceFactory(name, persistenceFactory);
  }
}
