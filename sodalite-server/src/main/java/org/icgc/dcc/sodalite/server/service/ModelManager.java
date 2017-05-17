package org.icgc.dcc.sodalite.server.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.icgc.dcc.sodalite.server.model.Donor;
import org.icgc.dcc.sodalite.server.model.File;
import org.icgc.dcc.sodalite.server.model.Sample;
import org.icgc.dcc.sodalite.server.model.SequencingRead;
import org.icgc.dcc.sodalite.server.model.SequencingReadDocument;
import org.icgc.dcc.sodalite.server.model.Specimen;
import org.icgc.dcc.sodalite.server.model.Study;
import org.icgc.dcc.sodalite.server.model.VariantCall;
import org.icgc.dcc.sodalite.server.repository.DonorRepository;
import org.icgc.dcc.sodalite.server.repository.FileRepository;
import org.icgc.dcc.sodalite.server.repository.SampleRepository;
import org.icgc.dcc.sodalite.server.repository.SequencingReadRepository;
import org.icgc.dcc.sodalite.server.repository.SpecimenRepository;
import org.icgc.dcc.sodalite.server.repository.StudyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

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
  public void register(String json) {
    // assumption: shouldn't be any JSON parsing issues by this point
    JsonNode root = jsonReader.readTree(json);

    JsonNode metadata = root.path("study");
    if (metadata.isMissingNode()) {
      throw new RuntimeException(String.format("Could not find metadata", root.textValue())); 
    }
    
    Study study = registerMetadata(metadata);
    
    JsonNode analysisObject = root.path("sequencingRead");
    if (analysisObject.isMissingNode()) {
      analysisObject = root.path("variantCall");
      /* 
         if (analysisObject.isMissingNode()) {
           throw new RuntimeException(String.format("Unrecognized Analysis Object %s", root.textValue()));
         } else {
           VariantCall vcf = registerVariantCall(analysisObject);
           for (File f : study.getDonor().getSpecimen().getSample().getFiles()) {
             variantCallFileAssociationService.associate(read, f);  
           }
         }
       */
    } else {
      SequencingRead read = registerSequencingRead(analysisObject);
      for (File f : study.getDonor().getSpecimen().getSample().getFiles()) {
        sequencingReadFileAssociationService.associate(read, f);
      }
    }
  }
  
  @SneakyThrows
  private SequencingRead registerSequencingRead(JsonNode analysisObject) {
    ObjectReader reader = new ObjectMapper().readerFor(SequencingRead.class);
    SequencingRead read = reader.readValue(analysisObject);
    
    sequencingReadService.create(read);
    return read;
  }

  private VariantCall registerVariantCall(JsonNode analysisObject) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * Create Metadata Records
   * 
   */
  @SneakyThrows
  private Study registerMetadata(JsonNode metadata) {
    ObjectReader reader = new ObjectMapper().readerFor(Study.class);
    Study study = reader.readValue(metadata);
    
    val donor = study.getDonor();
    registerDonor(donor);
    
    val specimen = donor.getSpecimen();
    registerSpecimen(specimen);
    
    val sample = specimen.getSample();
    registerSample(sample);
    
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
      // otherwise just skip if Spcimen already exists
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
  
  public SequencingReadDocument loadSequencingRead(String analysisId) {
    SequencingRead read = sequencingReadService.getById(analysisId);
    
    List<File> files = new ArrayList<File>();
    for (val fileId : sequencingReadFileAssociationService.getFileIds(analysisId)) {
      File file = fileService.getById(fileId);
      files.add(file);
    }
    
    if (files.isEmpty()) {
      throw new InvalidRepositoryDataException(String.format("No files found for Sequencing Read id %s", analysisId));
    }
    
    Study study = studyService.getStudy(read.getStudyId());
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
