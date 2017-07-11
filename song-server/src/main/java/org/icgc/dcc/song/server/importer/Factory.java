package org.icgc.dcc.song.server.importer;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.val;
import org.icgc.dcc.common.core.util.stream.Streams;
import org.icgc.dcc.song.server.importer.convert.Converters;
import org.icgc.dcc.song.server.importer.download.PortalDownloadIterator;
import org.icgc.dcc.song.server.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.server.importer.persistence.PersistenceFactory;
import org.icgc.dcc.song.server.importer.persistence.filerestorer.impl.ObjectFileRestorer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.icgc.dcc.common.core.util.stream.Streams.stream;
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
    return (ArrayList<PortalFileMetadata>) Streams.stream(downloader.iterator())
        .flatMap(Collection::stream)
        .map(Converters::convertToPortalFileMetadata)
        .collect(toList());
  }

  public static Stream<ObjectNode> download(PortalDownloadIterator downloadIterator){
    return stream(downloadIterator.iterator())
        .flatMap(Collection::stream);
  }

  private static <T> ArrayList<T> downloadAndConvert(PortalDownloadIterator downloadIterator,
      Function<ObjectNode, T> convertFunction){
    return (ArrayList<T>) download(downloadIterator)
        .map(convertFunction)
        .collect(toList());
  }

  public static PersistenceFactory<ArrayList<PortalFileMetadata>, String> fileDataFactory(){
    return createPersistenceFactory(OBJECT_FILE_RESTORER, Factory::downloadAndConvertPortalFiles);
  }


}
