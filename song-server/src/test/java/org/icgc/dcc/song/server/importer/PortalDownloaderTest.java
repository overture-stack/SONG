package org.icgc.dcc.song.server.importer;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.server.importer.convert.Converters;
import org.icgc.dcc.song.server.importer.model.PortalFileMetadata;
import org.junit.Test;

import java.util.Collection;
import java.util.stream.Collectors;

import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Streams.stream;
import static org.icgc.dcc.song.server.importer.Config.PORTAL_API;
import static org.icgc.dcc.song.server.importer.download.PortalDownloadIterator.createDefaultPortalDownloadIterator;
import static org.icgc.dcc.song.server.importer.download.urlgenerator.impl.DummyPortalUrlGenerator.createDummyPortalUrlGenerator;

@Slf4j
public class PortalDownloaderTest {

  @Test
  public void testFileDownload(){
    val urlGenerator = createDummyPortalUrlGenerator(PORTAL_API);
    int size = 0;
    int count = 0;
    for (val fileMetas : createDefaultPortalDownloadIterator(urlGenerator)){
      size+=fileMetas.size();
      log.info("Count: {}", ++count);
    }
    log.info("Size: {}", size);
    val portalFileMetadatas = stream(createDefaultPortalDownloadIterator(urlGenerator).iterator())
        .flatMap(Collection::stream)
        .map(Converters::convertToPortalFileMetadata)
        .collect(toImmutableList());
    val map = portalFileMetadatas.stream()
        .collect(Collectors.groupingBy(PortalFileMetadata::getRepoDataBundleId));
    log.info("done");



  }

}
