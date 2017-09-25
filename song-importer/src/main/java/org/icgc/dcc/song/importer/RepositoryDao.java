package org.icgc.dcc.song.importer;

import lombok.NonNull;
import lombok.val;
import org.icgc.dcc.song.importer.model.SampleSet;
import org.icgc.dcc.song.server.model.analysis.SequencingReadAnalysis;
import org.icgc.dcc.song.server.model.analysis.VariantCallAnalysis;
import org.icgc.dcc.song.server.model.entity.Donor;
import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.model.entity.Sample;
import org.icgc.dcc.song.server.model.entity.Specimen;
import org.icgc.dcc.song.server.model.entity.Study;
import org.icgc.dcc.song.server.repository.AnalysisRepository;
import org.icgc.dcc.song.server.repository.DonorRepository;
import org.icgc.dcc.song.server.repository.FileRepository;
import org.icgc.dcc.song.server.repository.InfoRepository;
import org.icgc.dcc.song.server.repository.SampleRepository;
import org.icgc.dcc.song.server.repository.SpecimenRepository;
import org.icgc.dcc.song.server.repository.StudyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.icgc.dcc.song.server.model.enums.InfoTypes.ANALYSIS;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.DONOR;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.FILE;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.SAMPLE;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.SEQUENCING_READ;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.SPECIMEN;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.STUDY;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.VARIANT_CALL;

@Component
public class RepositoryDao {

  @Autowired private StudyRepository studyRepository;
  @Autowired private DonorRepository donorRepository;
  @Autowired private SpecimenRepository specimenRepository;
  @Autowired private SampleRepository sampleRepository;
  @Autowired private FileRepository fileRepository;
  @Autowired private AnalysisRepository analysisRepository;
  @Autowired private InfoRepository infoRepository;

  public void createDonor(@NonNull Donor donor) {
    donorRepository.create(donor);
    infoRepository.create(donor.getDonorId(), DONOR.toString(), donor.getInfo());
  }

  public void createStudy(@NonNull Study study) {
    studyRepository.create(study.getStudyId(), study.getName(), study.getOrganization(), study.getDescription());
    infoRepository.create(study.getStudyId(), STUDY.toString(), study.getInfo());
  }

  public void createSpecimen(@NonNull Specimen specimen) {
    specimenRepository.create(specimen);
    infoRepository.create(specimen.getSpecimenId(), SPECIMEN.toString(), specimen.getInfo());
  }

  public void createSample(@NonNull Sample sample) {
    sampleRepository.create(sample);
    infoRepository.create(sample.getSampleId(), SAMPLE.toString(), sample.getInfo());
  }

  public void createSequencingReadAnalysis(@NonNull SequencingReadAnalysis analysis) {
    analysisRepository.createAnalysis(analysis);
    infoRepository.create(analysis.getAnalysisId(), ANALYSIS.toString(), analysis.getInfo());

    val sequencingRead = analysis.getExperiment();
    analysisRepository.createSequencingRead(sequencingRead);
    infoRepository.create(sequencingRead.getAnalysisId(), SEQUENCING_READ.toString(), sequencingRead.getInfo());
  }

  public void createVariantCallAnalysis(@NonNull VariantCallAnalysis analysis) {
    analysisRepository.createAnalysis(analysis);
    infoRepository.create(analysis.getAnalysisId(), ANALYSIS.toString(), analysis.getInfo());

    val sequencingRead = analysis.getExperiment();
    analysisRepository.createVariantCall(sequencingRead);
    infoRepository.create(sequencingRead.getAnalysisId(), VARIANT_CALL.toString(), sequencingRead.getInfo());
  }

  public void createSampleSet(@NonNull SampleSet sampleSet) {
    analysisRepository.addSample(sampleSet.getAnalysisId(), sampleSet.getSampleId());
  }

  public void createFile(@NonNull File file) {
    fileRepository.create(file);
    infoRepository.create(file.getObjectId(), FILE.toString(), file.getInfo());
  }

}
