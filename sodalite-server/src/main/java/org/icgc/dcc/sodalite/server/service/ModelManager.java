package org.icgc.dcc.sodalite.server.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.icgc.dcc.sodalite.server.model.AnalysisType;
import org.icgc.dcc.sodalite.server.model.Donor;
import org.icgc.dcc.sodalite.server.model.File;
import org.icgc.dcc.sodalite.server.model.Sample;
import org.icgc.dcc.sodalite.server.model.SequencingRead;
import org.icgc.dcc.sodalite.server.model.SequencingReadDocument;
import org.icgc.dcc.sodalite.server.model.Specimen;
import org.icgc.dcc.sodalite.server.model.Study;
import org.icgc.dcc.sodalite.server.model.VariantCall;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@NoArgsConstructor
public class ModelManager {

  @Autowired
  StudyService studyService;
  @Autowired
  DonorService donorService;
  @Autowired
  SpecimenService specimenService;
  @Autowired
  SampleService sampleService;
  @Autowired
  FileService fileService;
  @Autowired
  SequencingReadService sequencingReadService;
  @Autowired
  SequencingReadFileAssociationService sequencingReadFileAssociationService;
  
  //@Autowired
  //VariantCallService variantCallService;
  //@Autowired
  //VariantCallFilAssociationService variantCallFileAssociationService;
  
  private static ObjectReader jsonReader = new ObjectMapper().reader();
  
  /*
   * Persist object graph deserialized from JSON - should be complete 
   */
  @SneakyThrows
  public void persist(String studyId, String uploadId, String json) {
    // assumption: shouldn't be any JSON parsing issues by this point
    persist(studyId, uploadId, jsonReader.readTree(json));
  }

  /**
   * Registers JSON document in two steps:
   *   1.  Create records for hierarchy of key metadata: Donor -> Specimen -> Sample -> Files
   *   2a. Determine what kind of Analysis Object was submitted
   *   2b. Persist Analysis Object and Associate Files to Analysis Object
   *   3.  Associate key Metadata to Analysis Object
   * 
   * @param root
   */
  public void persist(String studyId, String uploadId, JsonNode root) {
    JsonNode metadata = root.path("study");
    if (metadata.isMissingNode()) {
      throw new RuntimeException(String.format("Could not find metadata", root.textValue())); 
    }
    
    Study study = registerMetadata(studyId, metadata);
    registerAnalysis(study, uploadId, root);
  }

  /**
   * Convert an Iterator into a Java 8 Stream
   */
  protected Stream<String> streamNodeKeys(JsonNode node) {
    Iterable<String> iterable = () -> node.fieldNames();
    return StreamSupport.stream(iterable.spliterator(), false);
  }
  
  protected AnalysisType identifyAnalysisType(JsonNode root) {    
    val keyStream = streamNodeKeys(root);
    List<String> nodeKeys = keyStream.collect(Collectors.toList());
    
    Optional<String> analysisTypeKey = nodeKeys.stream().filter(k -> AnalysisType.isAnalysisType(k)).findFirst();
    if (!analysisTypeKey.isPresent()) {
      // this should not happen - should be enforced by the JSON Schema and caught during initial validation
      String foundKeys = nodeKeys.stream().collect(Collectors.joining(", "));
      String msg = String.format("Could not identify Analysis Type from: %s", foundKeys);
      log.error(msg);
      throw new IllegalArgumentException(msg);
    }
    String test = analysisTypeKey.get();
    return AnalysisType.fromValue(test);
  }
  
  /**
   * Register Analysis Object to associated metadata (modeled as graph rooted in Study)
   * @param study
   * @param root
   */
  public void registerAnalysis(Study study, String uploadId, JsonNode root) {
    val analysisType = identifyAnalysisType(root);
    JsonNode analysisObject = root.path(analysisType.value());
    switch(analysisType) {
    case SEQUENCING_READ:
        registerSequencingRead(study, uploadId, analysisObject);
        break;
    case VARIANT_CALL:
        registerVariantCall(study, uploadId, analysisObject);
        break;
    case TUMOUR_NORMAL_PAIR:
      default:
        throw new java.lang.UnsupportedOperationException("Not yet supported");
    }
  }

  @SneakyThrows
  private SequencingRead registerSequencingRead(Study study, String uploadId, JsonNode analysisObject) {
    ObjectReader reader = new ObjectMapper().readerFor(SequencingRead.class);
    SequencingRead read = reader.readValue(analysisObject);
    read.setStudyId(study.getStudyId());
    read.setAnalysisSubmitterId(uploadId);
    sequencingReadService.create(read);
    
    // Step 3: create associations
    for (File f : study.getDonor().getSpecimen().getSample().getFiles()) {
      sequencingReadFileAssociationService.associate(read, f);
    }
    
    return read;
  }

  @SneakyThrows
  private VariantCall registerVariantCall(Study study, String uploadId, JsonNode analysisObject) {
    ObjectReader reader = new ObjectMapper().readerFor(VariantCall.class);
    VariantCall call = reader.readValue(analysisObject);
    call.setAnalysisSubmitterId(uploadId);
    
    // TODO: waiting for implementation
    // variantCallService.create(call);
    
    // Step 3: create associations
    for (File f : study.getDonor().getSpecimen().getSample().getFiles()) {
      // variantCallFileAssociationService.associate(call, f);
    }
    return call;
  }

  
  /*
   * Create Metadata Records - assumes fully-populated json hierarchy
   * 
   */
  @SneakyThrows
  private Study registerMetadata(String studyId, JsonNode metadata) {
    ObjectReader reader = new ObjectMapper().readerFor(Study.class);
    Study study = reader.readValue(metadata);
    study.setStudyId(studyId);
    study.propagateKeys();

    val donor = study.getDonor();
    registerDonor(donor);
    donor.propagateKeys();
    
    val specimen = donor.getSpecimen();
    registerSpecimen(specimen);
    specimen.propagateKeys();
    
    val sample = specimen.getSample();
    registerSample(sample);
    sample.propagateKeys();
    
    for (File f : sample.getFiles()) {
      registerFile(f);
    }
    return study;
  }
  
  public void registerDonor(Donor submittedDonor) {
    val existingDonor = donorService.findByBusinessKey(submittedDonor.getStudyId(), submittedDonor.getDonorSubmitterId());
    if (existingDonor != null) {
      if (!submittedDonor.equals(existingDonor)) {
        // report an error - submitted data is different 
        throw new IllegalArgumentException(String.format("Submitting different metadata for Donor in Study %s and with Submitter Id %s", submittedDonor.getStudyId(), submittedDonor.getDonorSubmitterId()));
      }
      // otherwise just skip if Donor already exists
    } else {
      donorService.create(submittedDonor);  
    }

  }
  
  public void registerSpecimen(Specimen submittedSpecimen) {
    val existingSpecimen = donorService.findByBusinessKey(submittedSpecimen.getStudyId(), submittedSpecimen.getSpecimenSubmitterId());
    if (existingSpecimen != null) {
      if (!submittedSpecimen.equals(existingSpecimen)) {
        // report an error - submitted data is different 
        throw new IllegalArgumentException(String.format("Submitting different metadata for Specimen in Study %s and with Submitter Id %s", submittedSpecimen.getStudyId(), submittedSpecimen.getSpecimenSubmitterId()));
      }
      // otherwise just skip if Specimen already exists
    } else {
      specimenService.create(submittedSpecimen);  
    }
    
  }
  
  public void registerSample(Sample submittedSample) {
    val existingSample = sampleService.findByBusinessKey(submittedSample.getStudyId(), submittedSample.getSampleSubmitterId());
    if (existingSample != null) {
      if (!submittedSample.equals(existingSample)) {
        // report an error - submitted data is different 
        throw new IllegalArgumentException(String.format("Submitting different metadata for Sample in Study %s and with Submitter Id %s", submittedSample.getStudyId(), submittedSample.getSampleSubmitterId()));
      }
      // otherwise just skip if Sample already exists
    } else {
      sampleService.create(submittedSample);
    }
  }
  
  public void registerFile(File submittedFile) {
    val existingFile = sampleService.findByBusinessKey(submittedFile.getStudyId(), submittedFile.getFileName());
    if (existingFile != null) {
      if (!submittedFile.equals(existingFile)) {
        // report an error - submitted data is different 
        throw new IllegalArgumentException(String.format("Submitting different metadata for Sample in Study %s and with File Name %s", submittedFile.getStudyId(), submittedFile.getFileName()));
      }
      // otherwise just skip if Sample already exists
    } else {
      fileService.create(submittedFile);  
    }
  }
  
  /*
   * Serialize to JSON
   * 
   */
  
  public SequencingReadDocument loadSequencingRead(String studyId, String analysisSubmitterId) {
    SequencingRead read = sequencingReadService.findByBusinessKey(studyId, analysisSubmitterId);
    
    List<File> files = new ArrayList<File>();
    for (val fileId : sequencingReadFileAssociationService.getFileIds(read.getAnalysisId())) {
      File file = fileService.getById(fileId);
      files.add(file);
    }
    
    if (files.isEmpty()) {
      throw new InvalidRepositoryDataException(String.format("No files found for Sequencing Read id %s", read.getAnalysisId()));
    }
    
    Study study = studyService.getStudy(read.getStudyId());
    
    // Sequencing Read assumes all files belong to same sample
    Sample sample = sampleService.getById(files.get(0).getSampleId());
    Specimen specimen = specimenService.getById(sample.getSpecimenId());
    Donor donor = donorService.getById(specimen.getDonorId());
    
    // assemble
    sample.withFiles(files);
    specimen.withSample(sample);
    donor.withSpecimen(specimen);
    study.withDonor(donor);
    
    SequencingReadDocument result = new SequencingReadDocument(read, study);
    
    return result;
  }

}
