package org.icgc.dcc.song.server.importer;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.icgc.dcc.song.server.importer.Config.PORTAL_API;
import static org.icgc.dcc.song.server.importer.Factory.DATA_CONTAINER_FILE_RESTORER;
import static org.icgc.dcc.song.server.importer.download.urlgenerator.impl.TotalFilesPortalUrlGenerator.createTotalFilesPortalUrlGenerator;
import static org.icgc.dcc.song.server.importer.persistence.PersistenceFactory.createPersistenceFactory;

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

    val dataFetcher = Factory.buildDataFetcher();
    val persistenceFactory = createPersistenceFactory(DATA_CONTAINER_FILE_RESTORER, dataFetcher::fetchData);
    val dataContainer = persistenceFactory.getObject("dataContainer.dat");
    log.info("doneeee");




    //Assert that the dataBundleId for Collab is the same as the repoDataBundleId

    //Assert there are only bams and VCFs (will throw exxception if something DNE
    /*
    val numNonBamOrVcfs = portalFileMetadatas.stream()
        .map(FileTypes::resolve)
        .filter(x -> (x != BAM) && (x != VCF))
        .count();
    assertThat(numNonBamOrVcfs).isEqualTo(0);
    */



    // Assert that a file response NEVER has more than one
    // sampleId, specimenId, submittedSampleId, and submittedSpecimenId
    /*
    val portalDonorIdFetcher = createPortalDonorIdFetcher(PORTAL_API);
    val donorDao = createDonorDao(portalDonorIdFetcher);
    val moreThan1SampleId = portalFileMetadatas.stream()
        .filter(x -> x.getSampleIds().size() != 1)
        .collect(toList());
    val moreThan1SpecimenId = portalFileMetadatas.stream()
        .filter(x -> x.getSpecimenIds().size() != 1)
        .collect(toList());
    val moreThan1SubmittedSampleId = portalFileMetadatas.stream()
        .filter(x -> x.getSubmittedSampleIds().size() != 1)
        .collect(toList());
    val moreThan1SubmittedSpecimenId = portalFileMetadatas.stream()
        .filter(x -> x.getSubmittedSpecimenIds().size() != 1)
        .collect(toList());
    assertThat(moreThan1SampleId).isEmpty();
    assertThat(moreThan1SubmittedSampleId).isEmpty();
    assertThat(moreThan1SpecimenId).isEmpty();
    assertThat(moreThan1SubmittedSpecimenId).isEmpty();
    */

    //Assert that for each LibraryStrategy, every specimenId:sampleId is 1:1
    /*
    val d = portalFileMetadatas.stream().collect(Collectors.groupingBy(x -> x.getSpecimenIds().get(0)));
    for (val entry : d.entrySet()){
      val specimenId = entry.getKey();
      val analysisMap = entry.getValue().stream().collect(Collectors.groupingBy(x -> x.getExperimentalStrategy()));
      for (val analysisEntry : analysisMap.entrySet()){
        val libraryStrategy = analysisEntry.getKey();
        val sampleIds = analysisEntry.getValue().stream().map(x -> x.getSampleIds().get(0)).collect(toSet());
        assertThat(sampleIds.size()).withFailMessage("SpecimenId: %s, LibraryStrategy: %s,  SampleIds: %s", specimenId, libraryStrategy, sampleIds).isEqualTo(1);
      }
    }
    */

    // Add realtime assertion that for each file, there is one donor, one specimen and one sample. This is only valid
    // for collab

    //rtism     portalFileMetadatas.forEach(x -> PortalDownloaderTest.something(x,donorDao));
    ///// Test to see if each portalFileMetadata, has:
    // - more than 1 donor
    // - an AND of the following:
    //   - more than 1 specimenId (same for submittedSpecimenId)
    //   - more than 1 sampleId (same for submittedSampleId)
    //   - more than 1 specimenType

    log.info("sdf");

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
