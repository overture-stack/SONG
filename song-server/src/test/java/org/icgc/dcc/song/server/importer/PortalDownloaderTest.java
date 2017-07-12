package org.icgc.dcc.song.server.importer;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.icgc.dcc.song.server.importer.convert.Converters;
import org.icgc.dcc.song.server.importer.dao.DonorDao;
import org.icgc.dcc.song.server.importer.model.PortalFileMetadata;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.icgc.dcc.song.server.importer.Config.PORTAL_API;
import static org.icgc.dcc.song.server.importer.dao.DonorDao.createDonorDao;
import static org.icgc.dcc.song.server.importer.download.PortalDownloadIterator.createDefaultPortalDownloadIterator;
import static org.icgc.dcc.song.server.importer.download.urlgenerator.impl.FilePortalUrlGenerator.createFilePortalUrlGenerator;
import static org.icgc.dcc.song.server.importer.download.PortalDonorIdFetcher.createPortalDonorIdFetcher;
import static org.icgc.dcc.song.server.importer.download.urlgenerator.impl.TotalFilesPortalUrlGenerator.createTotalFilesPortalUrlGenerator;

@Slf4j
public class PortalDownloaderTest {
  public static final Path PERSISTENCE_DIR_PATH = Paths.get("persistence");


  @Test
  @SneakyThrows
  public void testFileDownload(){
    val totalFilesUrlGenerator= createTotalFilesPortalUrlGenerator(PORTAL_API);
    val resp = JsonUtils.read(totalFilesUrlGenerator.getUrl(1,1));

    val fileCount =  resp.path("fileCount").asInt();
    val donorCount =  resp.path("donorCount").asInt();

    val url = createFilePortalUrlGenerator(PORTAL_API);
    val portalDownloader = createDefaultPortalDownloadIterator(url);
    val portalDonorIdFetcher = createPortalDonorIdFetcher(PORTAL_API);
    val donorDao = createDonorDao(portalDonorIdFetcher);

    portalDownloader.stream()
        .map(Converters::convertToPortalFileMetadata)
        .forEach(x -> PortalDownloaderTest.something(x,donorDao));

    log.info("sdf");

  }

  private static void something(PortalFileMetadata portalFileMetadata, DonorDao donorDao){
    val donorId = portalFileMetadata.getDonorId();
    val portalDonorMetadata = donorDao.getPortalDonorMetadata(donorId);
    log.info("DonorId: {}", donorId);
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
