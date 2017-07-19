package org.icgc.dcc.song.importer;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.importer.convert.SpecimenSampleConverter.SpecimenSampleTuple;
import org.icgc.dcc.song.importer.model.DataContainer;
import org.icgc.dcc.song.importer.model.PortalDonorMetadata;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.server.model.entity.Study;
import org.icgc.dcc.song.server.repository.AnalysisRepository;
import org.icgc.dcc.song.server.repository.DonorRepository;
import org.icgc.dcc.song.server.repository.FileRepository;
import org.icgc.dcc.song.server.repository.SampleRepository;
import org.icgc.dcc.song.server.repository.SpecimenRepository;
import org.icgc.dcc.song.server.repository.StudyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.icgc.dcc.song.importer.Factory.DONOR_CONVERTER;
import static org.icgc.dcc.song.importer.Factory.FILE_CONVERTER;
import static org.icgc.dcc.song.importer.Factory.FILE_SET_CONVERTER;
import static org.icgc.dcc.song.importer.Factory.SAMPLE_SET_CONVERTER;
import static org.icgc.dcc.song.importer.Factory.SPECIMEN_SAMPLE_CONVERTER;
import static org.icgc.dcc.song.importer.Factory.STUDY_CONVERTER;
import static org.icgc.dcc.song.importer.Factory.buildDataFetcher;
import static org.icgc.dcc.song.importer.Factory.buildFileFilter;
import static org.icgc.dcc.song.importer.Factory.buildPersistenceFactory;
import static org.icgc.dcc.song.importer.convert.AnalysisConverter.createAnalysisConverter;
import static org.icgc.dcc.song.importer.dao.DonorDao.createDonorDao;
import static org.icgc.dcc.song.importer.model.DataContainer.createDataContainer;

@Slf4j
@RequiredArgsConstructor
@Component
public class Importer implements  Runnable {

  private static final String DATA_CONTAINER_PERSISTENCE_FN = "dataContainer.dat";

  @Autowired private StudyRepository studyRepository;
  @Autowired private DonorRepository donorRepository;
  @Autowired private SpecimenRepository specimenRepository;
  @Autowired private SampleRepository sampleRepository;
  @Autowired private FileRepository fileRepository;
  @Autowired private AnalysisRepository analysisRepository;

  @Override
  public void run() {
    log.info("Building FileFilter...");
    val fileFilter = buildFileFilter();

    log.info("Building DataFetcher...");
    val dataFetcher = buildDataFetcher();

    log.info("Building PersistenceFactory...");
    val persistenceFactory = buildPersistenceFactory(dataFetcher::fetchData);

    log.info("Getting DataContainer object {}...", DATA_CONTAINER_PERSISTENCE_FN);
    val dataContainer = persistenceFactory.getObject(DATA_CONTAINER_PERSISTENCE_FN);


    log.info("Filtering input portalFileMetadataList with {} files", dataContainer.getPortalFileMetadataList().size());
    val filteredPortalFileMetadataList = fileFilter.passList(dataContainer.getPortalFileMetadataList());
    log.info("Filtered out {} files",
        dataContainer.getPortalFileMetadataList().size()-filteredPortalFileMetadataList.size());
    val filteredPortalDonorMetadataList = dataContainer.getPortalDonorMetadataList();
    val filteredDataContainer = createDataContainer(filteredPortalDonorMetadataList, filteredPortalFileMetadataList);
    processStudies(filteredDataContainer.getPortalDonorMetadataSet());
    processDonors(filteredDataContainer.getPortalDonorMetadataSet());
    processSpecimensAndSamples(filteredDataContainer.getPortalFileMetadataList());
    processFiles(filteredDataContainer.getPortalFileMetadataList());
    processAnalysis(filteredDataContainer);

  }

  private void processStudies( Set<PortalDonorMetadata> portalDonorMetadataSet){
    log.info("Converting Studies...");
    val studies = STUDY_CONVERTER.convertStudies(portalDonorMetadataSet);

    log.info("Updating StudyRepository with {} studies", studies.size());
    studies.forEach(this::createStudy);
  }

  private void createStudy(@NonNull Study study){
    studyRepository.create(study.getStudyId(),study.getName(),study.getOrganization(),study.getDescription());
  }

  private void processDonors(Set<PortalDonorMetadata> portalDonorMetadataSet){
    log.info("Converting donors...");
    val donors = DONOR_CONVERTER.convertDonors(portalDonorMetadataSet);

    log.info("Updating DonorRepository with {} donors", donors.size());
    donors.forEach(donorRepository::create);
  }

  private void processSpecimensAndSamples(List<PortalFileMetadata> portalFileMetadataList){

    log.info("Converting Specimen and Samples...");
    val specimenSampleTuples = SPECIMEN_SAMPLE_CONVERTER.convertSpecimenSampleTuples(portalFileMetadataList);

    // Aggregating specimens
    val specimens = specimenSampleTuples.stream()
        .collect(
            groupingBy(SpecimenSampleTuple::getSpecimen))
        .keySet();

    log.info("Updating SpecimenRepository with {} specimens", specimens.size());
    specimens.forEach(specimenRepository::create);

    // Aggregating samples
    val samples = specimenSampleTuples.stream()
        .collect(
            groupingBy(x -> x.getSample().getSampleId()))
        .entrySet()
        .stream()
        .map(x -> x.getValue().get(0).getSample()) //TODO: why??
        .collect(toImmutableSet());

    log.info("Updating SampleRepository with {} samples", samples.size());
    samples.forEach(sampleRepository::create);

  }

  private void processFiles(List<PortalFileMetadata> portalFileMetadataList){
    log.info("Converting Files...");
    val files = FILE_CONVERTER.convertFiles(portalFileMetadataList);

    log.info("Updating FileRepository with {} files", files.size());
    files.forEach(fileRepository::create);
  }

  private void processAnalysis(DataContainer dataContainer){
    log.info("Creating DonorDao...");
    val donorDao = createDonorDao(dataContainer.getPortalDonorMetadataList());

    log.info("Creating AnalysisConverter using DonorDao");
    val analysisConverter = createAnalysisConverter(donorDao);

    log.info("Converting SequencingReads...");
    val seqReadAnalysisList = analysisConverter.convertSequencingReads(dataContainer.getPortalFileMetadataList());

    log.info("Updating analysisRepository with {} Sequencing Reads", seqReadAnalysisList.size());
    seqReadAnalysisList.forEach(x -> {
      analysisRepository.createAnalysis(x);
      analysisRepository.createSequencingRead(x.getExperiment());
    });

    log.info("Converting VariantCalls...");
    val variantCallList = analysisConverter.convertVariantCalls(dataContainer.getPortalFileMetadataList());

    log.info("Updating analysisRepository with {} VariantCalls", variantCallList.size());
    variantCallList.forEach(x -> {
      analysisRepository.createAnalysis(x);
      analysisRepository.createVariantCall(x.getExperiment());
    });

    log.info("Converting FileSets...");
    val fileSets = FILE_SET_CONVERTER.convertFileSets(dataContainer.getPortalFileMetadataList());

    log.info("Updating analysisRespositry with {} FileSets", fileSets.size());
    fileSets.forEach(x -> analysisRepository.addFile(x.getAnalysisId(), x.getFileId()));

    log.info("Converting SampleSets...");
    val sampleSets = SAMPLE_SET_CONVERTER.convertSampleSets(dataContainer.getPortalFileMetadataList());

    log.info("Updating analysisRespositry with {} SampleSets", sampleSets.size());
    sampleSets.forEach(x -> analysisRepository.addSample(x.getAnalysisId(), x.getSampleId()));

  }

}
