package org.icgc.dcc.song.importer;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.importer.convert.SpecimenSampleConverter.SpecimenSampleTuple;
import org.icgc.dcc.song.importer.model.PortalDonorMetadata;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.importer.resolvers.FileTypes;
import org.icgc.dcc.song.server.model.entity.Donor;
import org.icgc.dcc.song.server.model.entity.Sample;
import org.icgc.dcc.song.server.model.entity.Specimen;
import org.icgc.dcc.song.server.model.entity.Study;
import org.icgc.dcc.song.server.repository.AnalysisRepository;
import org.icgc.dcc.song.server.repository.DonorRepository;
import org.icgc.dcc.song.server.repository.FileRepository;
import org.icgc.dcc.song.server.repository.SampleRepository;
import org.icgc.dcc.song.server.repository.SpecimenRepository;
import org.icgc.dcc.song.server.repository.StudyRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.icgc.dcc.song.importer.Config.PROBLEMATIC_SPECIMEN_IDS;
import static org.icgc.dcc.song.importer.Factory.DATA_CONTAINER_FILE_RESTORER;
import static org.icgc.dcc.song.importer.Factory.DONOR_CONVERTER;
import static org.icgc.dcc.song.importer.Factory.FILE_CONVERTER;
import static org.icgc.dcc.song.importer.Factory.FILE_SET_CONVERTER;
import static org.icgc.dcc.song.importer.Factory.SAMPLE_SET_CONVERTER;
import static org.icgc.dcc.song.importer.Factory.SPECIMEN_SAMPLE_CONVERTER;
import static org.icgc.dcc.song.importer.Factory.STUDY_CONVERTER;
import static org.icgc.dcc.song.importer.Factory.buildDataFetcher;
import static org.icgc.dcc.song.importer.Factory.buildFileFilter;
import static org.icgc.dcc.song.importer.convert.AnalysisConverter.createAnalysisConverter;
import static org.icgc.dcc.song.importer.dao.DonorDao.createDonorDao;
import static org.icgc.dcc.song.importer.persistence.PersistenceFactory.createPersistenceFactory;
import static org.icgc.dcc.song.importer.resolvers.FileTypes.BAM;
import static org.icgc.dcc.song.importer.resolvers.FileTypes.VCF;

@Slf4j
//@SpringBootTest
//@RunWith(SpringJUnit4ClassRunner.class)
//@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, FlywayTestExecutionListener.class })
//@FlywayTest
//@ActiveProfiles("dev")
public class PortalDownloaderTest {
  public static final Path PERSISTENCE_DIR_PATH = Paths.get("persistence");

  @Autowired private StudyRepository studyRepository;
  @Autowired private DonorRepository donorRepository;
  @Autowired private SpecimenRepository specimenRepository;
  @Autowired private SampleRepository sampleRepository;
  @Autowired private FileRepository fileRepository;
  @Autowired private AnalysisRepository analysisRepository;

  public static <T> Set<T> getSetDifference(Iterable<T> left, Iterable<T> right){
    val leftSet = newHashSet(left);
    val rightSet = newHashSet(right);
    leftSet.removeAll(rightSet);
    return leftSet;
  }

  public static <T> boolean setsHaveDifference(Iterable<T> left, Iterable<T> right){
    val leftSet = newHashSet(left);
    val rightSet = newHashSet(right);
    val leftContainsAll = leftSet.containsAll(rightSet);
    val rightContainsAll = rightSet.containsAll(leftSet);
    val isSame = leftContainsAll && rightContainsAll;
    return !isSame;
  }

  @Test
  public void testTest(){

  }

  @SneakyThrows
  public void testFileDownload(){
//    val totalFilesUrlGenerator= createTotalFilesPortalUrlGenerator(PORTAL_API);
//    val resp = JsonUtils.read(totalFilesUrlGenerator.getUrl(1,1));
//
//    val fileCount =  resp.path("fileCount").asInt();
//    val donorCount =  resp.path("donorCount").asInt();

    val fileFilter = buildFileFilter();
    log.info("Persisting or fetching data...");
    val dataFetcher = buildDataFetcher();
    val persistenceFactory = createPersistenceFactory(DATA_CONTAINER_FILE_RESTORER, dataFetcher::fetchData);
    val dataContainer = persistenceFactory.getObject("dataContainer.dat");
    val filteredPortalFileMetadataList = fileFilter.passList(dataContainer.getPortalFileMetadataList());
    val filteredPortalFileMetadataSet = newHashSet(filteredPortalFileMetadataList);
    val filteredPortalDonorMetadataList = dataContainer.getPortalDonorMetadataList();
    val filteredPortalDonorMetadataSet = dataContainer.getPortalDonorMetadataSet();
    val difference = getSetDifference(dataContainer.getPortalFileMetadataList(), filteredPortalFileMetadataList);

    val differentSpecimenIds = difference
        .stream()
        .map(PortalFileMetadata::getSpecimenIds)
        .flatMap(Collection::stream)
        .collect(toSet());
    assertThat(setsHaveDifference(differentSpecimenIds, PROBLEMATIC_SPECIMEN_IDS)).isFalse();



    val donorDao = createDonorDao(filteredPortalDonorMetadataList);
    val analysisConverter = createAnalysisConverter(donorDao);

    log.info("Converting donors...");
    val donors = DONOR_CONVERTER.convertDonors(filteredPortalDonorMetadataSet);

    log.info("Converting SequencingReads...");
    val seqReadAnalysisList = analysisConverter.convertSequencingReads(filteredPortalFileMetadataList);

    log.info("Converting VariantCalls...");
    val variantCallList = analysisConverter.convertVariantCalls(filteredPortalFileMetadataList);

    log.info("Converting Specimen and Samples...");
    val specimenSampleTuples = SPECIMEN_SAMPLE_CONVERTER.convertSpecimenSampleTuples(filteredPortalFileMetadataList);

    log.info("Converting Studies...");
    val studies = STUDY_CONVERTER.convertStudies(filteredPortalDonorMetadataSet);

    log.info("Converting Files...");
    val files = FILE_CONVERTER.convertFiles(filteredPortalFileMetadataList);

    log.info("Converting FileSets...");
    val fileSets = FILE_SET_CONVERTER.convertFileSets(filteredPortalFileMetadataList);

    log.info("Converting SampleSets...");
    val sampleSets = SAMPLE_SET_CONVERTER.convertSampleSets(filteredPortalFileMetadataList);

    log.info("sdf");


    //Assert that the portalDonorMetadata set has unique donors
    assertThat(filteredPortalDonorMetadataSet.stream().map(PortalDonorMetadata::getDonorId).collect(toSet()).size())
        .isEqualTo(filteredPortalDonorMetadataSet.size());
    assertThat(filteredPortalDonorMetadataList.stream().map(PortalDonorMetadata::getDonorId).collect(toSet()).size())
        .isEqualTo(filteredPortalDonorMetadataList.size());

    //Assert that the portalFileMetadata has unique files
    assertThat(filteredPortalFileMetadataSet.stream().map(PortalFileMetadata::getDonorId).collect(toSet()).size())
        .isEqualTo(filteredPortalDonorMetadataSet.size());
    assertThat(filteredPortalFileMetadataList.stream().map(PortalFileMetadata::getDonorId).collect(toSet()).size())
        .isEqualTo(filteredPortalDonorMetadataList.size());



    //Assert Donor set is unique
    assertThat(donors.size()).isEqualTo(donors.stream().map(Donor::getDonorId).collect(toSet()).size());


    // Assert that the total number of provided donors (provided becuase there were some donors that were
    // rejected) is that same a the number of uniqe donor ids taken from:
    // 1. collection of PortalDonorMetadatas
    // 2. collection of converterd Donor entities
    val expectedNumDonors = filteredPortalDonorMetadataList.stream()
        .map(PortalDonorMetadata::getDonorId).collect(toSet()).size();
    val actualNumPortalDonorsSet = filteredPortalDonorMetadataSet.size();
    val actualNumUniquePortalDonorsList = filteredPortalDonorMetadataList.stream()
        .map(PortalDonorMetadata::getDonorId).collect(toSet()).size();
    val actualNumConvertedDonors = donors.size();

    assertThat(actualNumPortalDonorsSet).isEqualTo(expectedNumDonors);
    assertThat(actualNumUniquePortalDonorsList).isEqualTo(expectedNumDonors);
    assertThat(actualNumConvertedDonors).isEqualTo(expectedNumDonors);


    // Assert number of files
    val expectedNumFiles  = filteredPortalFileMetadataSet.size();
    assertThat(files.size()).isEqualTo(expectedNumFiles);
    assertThat(fileSets.size()).isEqualTo(expectedNumFiles);

    // Assert number of specimens
    val expectedNumSpecimens = filteredPortalFileMetadataList.stream()
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
    val expectedNumSamples= filteredPortalFileMetadataList.stream()
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
    val numNonBamOrVcfs = filteredPortalFileMetadataList.stream()
        .map(FileTypes::resolve)
        .filter(x -> (x != BAM) && (x != VCF))
        .count();
    assertThat(numNonBamOrVcfs).isEqualTo(0);



    // Assert that a file response NEVER has more than one
    // sampleId, specimenId, submittedSampleId, and submittedSpecimenId
    val moreThan1SampleId = filteredPortalFileMetadataList.stream()
        .filter(x -> x.getSampleIds().size() != 1)
        .collect(toList());
    val moreThan1SpecimenId = filteredPortalFileMetadataList.stream()
        .filter(x -> x.getSpecimenIds().size() != 1)
        .collect(toList());
    val moreThan1SubmittedSampleId = filteredPortalFileMetadataList.stream()
        .filter(x -> x.getSubmittedSampleIds().size() != 1)
        .collect(toList());
    val moreThan1SubmittedSpecimenId = filteredPortalFileMetadataList.stream()
        .filter(x -> x.getSubmittedSpecimenIds().size() != 1)
        .collect(toList());
    assertThat(moreThan1SampleId).isEmpty();
    assertThat(moreThan1SubmittedSampleId).isEmpty();
    assertThat(moreThan1SpecimenId).isEmpty();
    assertThat(moreThan1SubmittedSpecimenId).isEmpty();

    //Assert that for each LibraryStrategy, every specimenId:sampleId is 1:1
    val d = filteredPortalFileMetadataList.stream().collect(groupingBy(x -> x.getSpecimenIds().get(0)));
    for (val entry : d.entrySet()){
      val specimenId = entry.getKey();
      val analysisMap = entry.getValue().stream().collect(groupingBy(x -> x.getExperimentalStrategy()));
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

    studies.forEach(this::createStudy);
    donors.forEach(donorRepository::create);
    val specimens = specimenSampleTuples.stream()
        .collect(
            groupingBy(SpecimenSampleTuple::getSpecimen))
        .keySet();

    specimens.forEach(specimenRepository::create);

    val samples = specimenSampleTuples.stream()
        .collect(
            groupingBy(x -> x.getSample().getSampleId()))
        .entrySet()
        .stream()
        .map(x -> x.getValue().get(0).getSample())
        .collect(toImmutableSet());
    samples.forEach(sampleRepository::create);
    files.forEach(fileRepository::create);
    seqReadAnalysisList.forEach(x -> {
      analysisRepository.createAnalysis(x);
      analysisRepository.createSequencingRead(x.getExperiment());
    });
    variantCallList.forEach(x -> {
      analysisRepository.createAnalysis(x);
      analysisRepository.createVariantCall(x.getExperiment());
    });

    fileSets.forEach(x -> analysisRepository.addFile(x.getAnalysisId(), x.getFileId()));
    sampleSets.forEach(x -> analysisRepository.addSample(x.getAnalysisId(), x.getSampleId()));



    log.info("sdf");

  }

  private void createStudy(Study study){
    studyRepository.create(study.getStudyId(),study.getName(),study.getOrganization(),study.getDescription());
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
