/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.icgc.dcc.song.importer;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.importer.model.SampleSet;
import org.icgc.dcc.song.server.model.analysis.Analysis;
import org.icgc.dcc.song.server.model.analysis.SequencingReadAnalysis;
import org.icgc.dcc.song.server.model.analysis.VariantCallAnalysis;
import org.icgc.dcc.song.server.model.entity.Donor;
import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.model.entity.Sample;
import org.icgc.dcc.song.server.model.entity.Specimen;
import org.icgc.dcc.song.server.model.entity.Study;
import org.icgc.dcc.song.server.model.experiment.SequencingRead;
import org.icgc.dcc.song.server.model.experiment.VariantCall;
import org.icgc.dcc.song.server.repository.AnalysisRepository;
import org.icgc.dcc.song.server.repository.DonorRepository;
import org.icgc.dcc.song.server.repository.FileRepository;
import org.icgc.dcc.song.server.repository.InfoRepository;
import org.icgc.dcc.song.server.repository.SampleRepository;
import org.icgc.dcc.song.server.repository.SpecimenRepository;
import org.icgc.dcc.song.server.repository.StudyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Function;

import static java.util.Objects.isNull;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.ANALYSIS;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.DONOR;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.FILE;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.SAMPLE;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.SEQUENCING_READ;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.SPECIMEN;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.STUDY;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.VARIANT_CALL;

@Component
@Slf4j
public class RepositoryDao {

  @Autowired private StudyRepository studyRepository;
  @Autowired private DonorRepository donorRepository;
  @Autowired private SpecimenRepository specimenRepository;
  @Autowired private SampleRepository sampleRepository;
  @Autowired private FileRepository fileRepository;
  @Autowired private AnalysisRepository analysisRepository;
  @Autowired private InfoRepository infoRepository;

  public boolean hasAnalysis(Analysis analysis){
    return !isNull(analysisRepository.read(analysis.getAnalysisId()));
  }

  public boolean hasSequencingRead(SequencingRead sequencingRead){
    return !isNull(analysisRepository.readSequencingRead(sequencingRead.getAnalysisId()));
  }

  public boolean hasVariantCall(VariantCall variantCall){
    return !isNull(analysisRepository.readVariantCall(variantCall.getAnalysisId()));
  }

  public boolean hasDonor(@NonNull Donor donor){
    return !isNull(donorRepository.read(donor.getDonorId()));
  }

  public boolean hasStudy(@NonNull Study study){
    return !isNull(studyRepository.read(study.getStudyId()));
  }

  public boolean hasSpecimen(@NonNull Specimen specimen){
    return !isNull(specimenRepository.read(specimen.getSpecimenId()));
  }

  public boolean hasSample(@NonNull Sample sample){
    return !isNull(sampleRepository.read(sample.getSampleId()));
  }

  private static <T> void logUpdate(Function<T,Integer> updateFunction, T object){
    val result = updateFunction.apply(object);
    if (result == 1){
      log.warn("Updated '{}' entity: {}",
          object.getClass().getSimpleName(), object);
    } else {
      log.info("NOT Updating '{}' entity: {}",
          object.getClass().getSimpleName(), object);
    }
  }

  private void logDonorUpdate(Donor donor){
    logUpdate(donorRepository::update, donor);
  }

  private void logStudyUpdate(Study study){
    logUpdate(x -> studyRepository.set(x.getStudyId(), x.getName(), x.getDescription()), study);
  }

  private void logSampleUpdate(Sample sample){
    logUpdate(sampleRepository::update, sample);
  }

  private void logSpecimenUpdate(Specimen specimen){
    logUpdate(specimenRepository::update, specimen);
  }

  private void logFileUpdate(File file){
    logUpdate(fileRepository::update, file);
  }

  public void createDonor(@NonNull Donor donor) {
    if (hasDonor(donor)){
      logDonorUpdate(donor);
      infoRepository.set(donor.getDonorId(), DONOR.toString(), donor.getInfoAsString());
    } else {
      donorRepository.create(donor);
      infoRepository.create(donor.getDonorId(), DONOR.toString(), donor.getInfoAsString());
    }
  }

  public void createStudy(@NonNull Study study) {
    if (hasStudy(study)){
      logStudyUpdate(study);
      infoRepository.set(study.getStudyId(), STUDY.toString(), study.getInfoAsString());
    } else {
      studyRepository.create(study.getStudyId(), study.getName(), study.getOrganization(), study.getDescription());
      infoRepository.create(study.getStudyId(), STUDY.toString(), study.getInfoAsString());
    }
  }

  public void createSpecimen(@NonNull Specimen specimen) {
    if (hasSpecimen(specimen)){
      logSpecimenUpdate(specimen);
      infoRepository.set(specimen.getSpecimenId(), SPECIMEN.toString(), specimen.getInfoAsString());
    } else {
      specimenRepository.create(specimen);
      infoRepository.create(specimen.getSpecimenId(), SPECIMEN.toString(), specimen.getInfoAsString());
    }
  }

  public void createSample(@NonNull Sample sample) {
    if (hasSample(sample)){
      logSampleUpdate(sample);
      infoRepository.set(sample.getSampleId(), SAMPLE.toString(), sample.getInfoAsString());
    } else {
      sampleRepository.create(sample);
      infoRepository.create(sample.getSampleId(), SAMPLE.toString(), sample.getInfoAsString());
    }
  }


  public void createSequencingReadAnalysis(@NonNull SequencingReadAnalysis analysis) {
    val sequencingRead = analysis.getExperiment();
    val hasAnalysis = hasAnalysis(analysis);
    val hasSequencingRead = hasSequencingRead(sequencingRead);
    if (!hasAnalysis) {
      analysisRepository.createAnalysis(analysis);
      infoRepository.create(analysis.getAnalysisId(), ANALYSIS.toString(), analysis.getInfoAsString());
    }
    if (hasSequencingRead){
      log.info("Attempting to update SequencingRead: {}", sequencingRead);
      analysisRepository.updateSequencingRead(sequencingRead);
      infoRepository.set(sequencingRead.getAnalysisId(), SEQUENCING_READ.toString(), sequencingRead.getInfoAsString());
    } else {
      analysisRepository.createSequencingRead(sequencingRead);
      infoRepository.create(sequencingRead.getAnalysisId(), SEQUENCING_READ.toString(), sequencingRead.getInfoAsString());
    }
  }

  public void createVariantCallAnalysis(@NonNull VariantCallAnalysis analysis) {
    val variantCall = analysis.getExperiment();
    if (!hasAnalysis(analysis)){
      analysisRepository.createAnalysis(analysis);
      infoRepository.create(analysis.getAnalysisId(), ANALYSIS.toString(), analysis.getInfoAsString());
    }
    if (hasVariantCall(variantCall)){
      log.info("Attempting to update VariantCall: {}", variantCall);
      analysisRepository.updateVariantCall(variantCall);
      infoRepository.set(variantCall.getAnalysisId(), VARIANT_CALL.toString(), variantCall.getInfoAsString());

    } else {
      analysisRepository.createVariantCall(variantCall);
      infoRepository.create(variantCall.getAnalysisId(), VARIANT_CALL.toString(), variantCall.getInfoAsString());
    }
  }

  @Deprecated
  public void updateVariantCallAnalysis(@NonNull VariantCallAnalysis analysis) {
    if (!hasAnalysis(analysis)){
      log.info("Creating new analysis {}", analysis.getAnalysisId());
      analysisRepository.createAnalysis(analysis);
      infoRepository.create(analysis.getAnalysisId(), ANALYSIS.toString(), analysis.getInfoAsString());
    } else {
      infoRepository.set(analysis.getAnalysisId(), ANALYSIS.toString(), analysis.getInfoAsString());
    }

    val variantCall = analysis.getExperiment();
    if(!hasVariantCall(variantCall)){
      log.info("Creating new variantCall {}", variantCall.getAnalysisId());
      analysisRepository.createVariantCall(variantCall);
      infoRepository.create(variantCall.getAnalysisId(), VARIANT_CALL.toString(), variantCall.getInfoAsString());
    } else {
      val storedVC = analysisRepository.readVariantCall(variantCall.getAnalysisId());
      if (!storedVC.equals(variantCall)){
        log.warn("UPDATING variantCall {}", variantCall.getAnalysisId());
        analysisRepository.updateVariantCall(variantCall);
        infoRepository.set(variantCall.getAnalysisId(), VARIANT_CALL.toString(), variantCall.getInfoAsString());
      }
    }

  }

  private boolean hasFile(File file){
    return !isNull(fileRepository.read(file.getObjectId()));
  }

  private boolean hasSampleSet(SampleSet sampleSet){
    return !isNull(analysisRepository.read(sampleSet.getAnalysisId()))
        && !isNull(sampleRepository.read(sampleSet.getSampleId()));
  }

  public void createSampleSet(@NonNull SampleSet sampleSet) {
    if (!hasSampleSet(sampleSet)){
      analysisRepository.addSample(sampleSet.getAnalysisId(), sampleSet.getSampleId());
    }
  }

  public void createFile(@NonNull File file) {
    if (hasFile(file)){
      logFileUpdate(file);
      infoRepository.set(file.getObjectId(), FILE.toString(), file.getInfoAsString());
    } else {
      fileRepository.create(file);
      infoRepository.create(file.getObjectId(), FILE.toString(), file.getInfoAsString());
    }
  }

}
