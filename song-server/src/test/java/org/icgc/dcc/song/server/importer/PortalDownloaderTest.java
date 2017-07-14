package org.icgc.dcc.song.server.importer;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.server.importer.convert.SpecimenSampleConverter.SpecimenSampleTuple;
import org.icgc.dcc.song.server.importer.dao.DonorDao;
import org.icgc.dcc.song.server.importer.model.PortalDonorMetadata;
import org.icgc.dcc.song.server.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.server.importer.resolvers.FileTypes;
import org.icgc.dcc.song.server.model.entity.Donor;
import org.icgc.dcc.song.server.model.entity.Sample;
import org.icgc.dcc.song.server.model.entity.Specimen;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.server.importer.Factory.DATA_CONTAINER_FILE_RESTORER;
import static org.icgc.dcc.song.server.importer.Factory.DONOR_CONVERTER;
import static org.icgc.dcc.song.server.importer.Factory.FILE_CONVERTER;
import static org.icgc.dcc.song.server.importer.Factory.FILE_SET_CONVERTER;
import static org.icgc.dcc.song.server.importer.Factory.SAMPLE_SET_CONVERTER;
import static org.icgc.dcc.song.server.importer.Factory.SPECIMEN_SAMPLE_CONVERTER;
import static org.icgc.dcc.song.server.importer.Factory.STUDY_CONVERTER;
import static org.icgc.dcc.song.server.importer.Factory.buildDataFetcher;
import static org.icgc.dcc.song.server.importer.convert.AnalysisConverter.createAnalysisConverter;
import static org.icgc.dcc.song.server.importer.persistence.PersistenceFactory.createPersistenceFactory;
import static org.icgc.dcc.song.server.importer.resolvers.FileTypes.BAM;
import static org.icgc.dcc.song.server.importer.resolvers.FileTypes.VCF;

@Slf4j
public class PortalDownloaderTest {
  public static final Path PERSISTENCE_DIR_PATH = Paths.get("persistence");

  @Test
  @SneakyThrows
  public void testFileDownload(){
//    val totalFilesUrlGenerator= createTotalFilesPortalUrlGenerator(PORTAL_API);
//    val resp = JsonUtils.read(totalFilesUrlGenerator.getUrl(1,1));
//
//    val fileCount =  resp.path("fileCount").asInt();
//    val donorCount =  resp.path("donorCount").asInt();

    log.info("Persisting or fetching data...");
    val dataFetcher = buildDataFetcher();
    val persistenceFactory = createPersistenceFactory(DATA_CONTAINER_FILE_RESTORER, dataFetcher::fetchData);
    val dataContainer = persistenceFactory.getObject("dataContainer.dat");
    val donorDao = DonorDao.createDonorDao(dataContainer.getPortalDonorMetadataList());
    val analysisConverter = createAnalysisConverter(donorDao);

    log.info("Converting donors...");
    val donors = DONOR_CONVERTER.convertDonors(dataContainer.getPortalDonorMetadataSet());

    log.info("Converting SequencingReads...");
    val seqReadAnalysisList = analysisConverter.convertSequencingReads(dataContainer.getPortalFileMetadataList());

    log.info("Converting VariantCalls...");
    val variantCallList = analysisConverter.convertVariantCalls(dataContainer.getPortalFileMetadataList());

    log.info("Converting Specimen and Samples...");
    val specimenSampleTuples = SPECIMEN_SAMPLE_CONVERTER.convertSpecimenSampleTuples(dataContainer.getPortalFileMetadataList());

    log.info("Converting Studies...");
    val studies = STUDY_CONVERTER.convertStudies(dataContainer.getPortalDonorMetadataSet());

    log.info("Converting Files...");
    val files = FILE_CONVERTER.convertFiles(dataContainer.getPortalFileMetadataList());

    log.info("Converting FileSets...");
    val fileSets = FILE_SET_CONVERTER.convertFileSets(dataContainer.getPortalFileMetadataList());

    log.info("Converting SampleSets...");
    val sampleSets = SAMPLE_SET_CONVERTER.convertSampleSets(dataContainer.getPortalFileMetadataList());

    log.info("sdf");

    val portalDonorMetadataList = dataContainer.getPortalDonorMetadataList();
    val portalDonorMetadataSet = dataContainer.getPortalDonorMetadataSet();
    val portalFileMetadataList = dataContainer.getPortalFileMetadataList();
    val portalFileMetadataSet = dataContainer.getPortalFileMetadataSet();

    //Assert that the portalDonorMetadata set has unique donors
    assertThat(portalDonorMetadataSet.stream().map(PortalDonorMetadata::getDonorId).collect(toSet()).size()).isEqualTo(dataContainer
        .getPortalDonorMetadataSet().size());
    assertThat(portalDonorMetadataList.stream().map(PortalDonorMetadata::getDonorId).collect(toSet()).size()).isEqualTo
        (dataContainer.getPortalDonorMetadataList().size());

    //Assert that the portalFileMetadata has unique files
    assertThat(portalFileMetadataSet.stream().map(PortalFileMetadata::getDonorId).collect(toSet()).size())
        .isEqualTo(portalDonorMetadataSet.size());
    assertThat(portalFileMetadataList.stream().map(PortalFileMetadata::getDonorId).collect(toSet()).size())
        .isEqualTo(portalDonorMetadataList.size());



    //Assert Donor set is unique
    assertThat(donors.size()).isEqualTo(donors.stream().map(Donor::getDonorId).collect(toSet()).size());


    // Assert that the total number of provided donors (provided becuase there were some donors that were
    // rejected) is that same a the number of uniqe donor ids taken from:
    // 1. collection of PortalDonorMetadatas
    // 2. collection of converterd Donor entities
    val expectedNumDonors = dataContainer.getPortalDonorMetadataList().stream()
        .map(PortalDonorMetadata::getDonorId).collect(toSet()).size();
    val actualNumPortalDonorsSet = dataContainer.getPortalDonorMetadataSet().size();
    val actualNumUniquePortalDonorsList = dataContainer.getPortalDonorMetadataList().stream()
        .map(PortalDonorMetadata::getDonorId).collect(toSet()).size();
    val actualNumConvertedDonors = donors.size();

    assertThat(actualNumPortalDonorsSet).isEqualTo(expectedNumDonors);
    assertThat(actualNumUniquePortalDonorsList).isEqualTo(expectedNumDonors);
    assertThat(actualNumConvertedDonors).isEqualTo(expectedNumDonors);


    // Assert number of files
    val expectedNumFiles  = portalFileMetadataSet.size();
    assertThat(files.size()).isEqualTo(expectedNumFiles);
    assertThat(fileSets.size()).isEqualTo(expectedNumFiles);

    // Assert number of specimens
    val expectedNumSpecimens = portalFileMetadataList.stream()
        .map(PortalFileMetadata::getSpecimenIds)
        .flatMap(Collection::stream)
        .collect(toSet())
        .size();
    val actualSpecimens = specimenSampleTuples.stream()
        .map(SpecimenSampleTuple::getSpecimen)
        .map(Specimen::getSpecimenId)
        .collect(toSet())
        .size();
    assertThat(actualSpecimens).isEqualTo(expectedNumSpecimens);

    // Assert number of specimens
    val expectedNumSamples= portalFileMetadataList.stream()
        .map(PortalFileMetadata::getSampleIds)
        .flatMap(Collection::stream)
        .collect(toSet())
        .size();
    val actualSamples = specimenSampleTuples.stream()
        .map(SpecimenSampleTuple::getSample)
        .map(Sample::getSampleId)
        .collect(toSet())
        .size();
    assertThat(actualSamples).isEqualTo(expectedNumSamples);






    //Assert that the dataBundleId for Collab is the same as the repoDataBundleId


    //Assert there are only bams and VCFs (will throw exxception if something DNE
    val numNonBamOrVcfs = dataContainer.getPortalFileMetadataList().stream()
        .map(FileTypes::resolve)
        .filter(x -> (x != BAM) && (x != VCF))
        .count();
    assertThat(numNonBamOrVcfs).isEqualTo(0);



    // Assert that a file response NEVER has more than one
    // sampleId, specimenId, submittedSampleId, and submittedSpecimenId
    val portalFileMetadatas = dataContainer.getPortalFileMetadataList();
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

    //Assert that for each LibraryStrategy, every specimenId:sampleId is 1:1
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
