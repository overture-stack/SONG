package org.icgc.dcc.song.server.importer;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.server.importer.download.PortalDownloadIterator;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.icgc.dcc.song.server.importer.Config.PORTAL_API;
import static org.icgc.dcc.song.server.importer.download.urlgenerator.impl.TotalFilesPortalUrlGenerator.createTotalFilesPortalUrlGenerator;

@Slf4j
public class PortalDownloaderTest {
  public static final Path PERSISTENCE_DIR_PATH = Paths.get("persistence");


  @Test
  @SneakyThrows
  public void testFileDownload(){
    val totalFilesUrlGenerator= createTotalFilesPortalUrlGenerator(PORTAL_API);
    val resp = PortalDownloadIterator.read(totalFilesUrlGenerator.getUrl(1,1));

    val fileCount =  resp.path("fileCount").asInt();
    val donorCount =  resp.path("donorCount").asInt();
    val fileFactory = Factory.fileDataFactory();
    val portalFileMetadatas = fileFactory.getObject("portalFileMetadatas.dat");
    log.info("Size: {}", portalFileMetadatas.size());





  }

  @RequiredArgsConstructor
  public static class Sleeper implements Runnable{

    private final String name;
    private final long msDelay;

    @SneakyThrows
    @Override public void run() {
      log.info("Waiting {} ms for {}", msDelay, name);
      Thread.sleep(msDelay);
      log.info("Done waiting for {}", name);
    }

  }

}
