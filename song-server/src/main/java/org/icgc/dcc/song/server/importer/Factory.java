package org.icgc.dcc.song.server.importer;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.val;
import org.icgc.dcc.song.server.importer.convert.Converters;
import org.icgc.dcc.song.server.importer.download.PortalDownloadIterator;
import org.icgc.dcc.song.server.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.server.importer.persistence.PersistenceFactory;
import org.icgc.dcc.song.server.importer.persistence.filerestorer.impl.ObjectFileRestorer;

import java.util.ArrayList;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static org.icgc.dcc.song.server.importer.Config.PERSISTED_DIR_PATH;
import static org.icgc.dcc.song.server.importer.Config.PORTAL_API;
import static org.icgc.dcc.song.server.importer.download.PortalDownloadIterator.createDefaultPortalDownloadIterator;
import static org.icgc.dcc.song.server.importer.download.urlgenerator.impl.FilePortalUrlGenerator.createFilePortalUrlGenerator;
import static org.icgc.dcc.song.server.importer.persistence.PersistenceFactory.createPersistenceFactory;
import static org.icgc.dcc.song.server.importer.persistence.filerestorer.impl.ObjectFileRestorer.createObjectFileRestorer;

public class Factory {

  private static final Class<ArrayList<PortalFileMetadata>> CLAZZ = (Class)ArrayList.class;
  private static final ObjectFileRestorer<ArrayList<PortalFileMetadata>> OBJECT_FILE_RESTORER =
      createObjectFileRestorer (PERSISTED_DIR_PATH, CLAZZ);

  private static PortalDownloadIterator buildFilePortalDownloader(){
    val urlGen = createFilePortalUrlGenerator(PORTAL_API);
    return createDefaultPortalDownloadIterator(urlGen);
  }

  private static ArrayList<PortalFileMetadata> downloadAndConvertPortalFiles(){
    val downloader = buildFilePortalDownloader();
    return (ArrayList<PortalFileMetadata>) downloader.stream()
        .map(Converters::convertToPortalFileMetadata)
        .collect(toList());
  }

  private static <T> ArrayList<T> downloadAndConvert(PortalDownloadIterator downloadIterator,
      Function<ObjectNode, T> convertFunction){
    return (ArrayList<T>) downloadIterator.stream()
        .map(convertFunction)
        .collect(toList());
  }

  public static PersistenceFactory<ArrayList<PortalFileMetadata>, String> fileDataFactory(){
    return createPersistenceFactory(OBJECT_FILE_RESTORER, Factory::downloadAndConvertPortalFiles);
  }


}
