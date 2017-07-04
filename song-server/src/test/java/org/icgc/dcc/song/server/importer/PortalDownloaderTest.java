package org.icgc.dcc.song.server.importer;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;

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

  }

}
