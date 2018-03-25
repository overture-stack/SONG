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
package org.icgc.dcc.song.server.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.server.model.entity.Sample;
import org.icgc.dcc.song.server.repository.SampleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Objects.isNull;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SAMPLE_ALREADY_EXISTS;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SAMPLE_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SAMPLE_ID_IS_CORRUPTED;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SAMPLE_REPOSITORY_CREATE_RECORD;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SAMPLE_REPOSITORY_DELETE_RECORD;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SAMPLE_REPOSITORY_UPDATE_RECORD;
import static org.icgc.dcc.song.core.exceptions.ServerException.checkServer;
import static org.icgc.dcc.song.core.utils.Responses.OK;

@Slf4j
@RequiredArgsConstructor
@Service
public class SampleService {

  @Autowired
  private final SampleRepository repository;

  @Autowired
  private final SampleInfoService infoService;

  @Autowired
  private final IdService idService;

  @Autowired
  private final StudyService studyService;

  private String createSampleId(String studyId, Sample sample){
    studyService.checkStudyExist(studyId);
    val inputSampleId = sample.getSampleId();
    val id = idService.generateSampleId(sample.getSampleSubmitterId(), studyId);
    checkServer(isNullOrEmpty(inputSampleId) || id.equals(inputSampleId), getClass(),
        SAMPLE_ID_IS_CORRUPTED,
        "The input sampleId '%s' is corrupted because it does not match the idServices sampleId '%s'",
        inputSampleId, id);
    checkSampleDoesNotExist(id);
    return id;
  }

  //TODO: [Related to SONG-260] should we add a specimenService.checkSpecimenExists(sample.getSpecimenId()) here?
  public String create(@NonNull String studyId, @NonNull Sample sample) {
    val id = createSampleId(studyId, sample);
    sample.setSampleId(id);
    sample.setSpecimenId(sample.getSpecimenId());
    int status = repository.create(sample);
    checkServer(status == 1,this.getClass(),
        SAMPLE_REPOSITORY_CREATE_RECORD, "Cannot create Sample: %s", sample.toString());
    infoService.create(id, sample.getInfoAsString());
    return id;
  }

  public Sample read(@NonNull String id) {
    val sample = repository.read(id);
    checkServer(!isNull(sample), getClass(), SAMPLE_DOES_NOT_EXIST,
        "The sample for sampleId '%s' could not be read because it does not exist", id);
    sample.setInfo(infoService.readNullableInfo(id));
    return sample;
  }

  List<Sample> readByParentId(@NonNull String parentId) {
    return repository.readByParentId(parentId);
  }

  public String update(@NonNull Sample sample) {
    checkSampleExists(sample.getSampleId());
    val status = repository.update(sample);
    checkServer(status == 1, getClass(), SAMPLE_REPOSITORY_UPDATE_RECORD,
        "Cannot update sampleId '%s' for sample '%s'", sample.getSampleId(), sample);
    infoService.update(sample.getSampleId(), sample.getInfoAsString());
    return OK;
  }

  public String delete(@NonNull String id) {
    checkSampleExists(id);
    val status = repository.delete(id);
    checkServer(status == 1, getClass(), SAMPLE_REPOSITORY_DELETE_RECORD,
        "Cannot delete sample with sampleId '%s'", id);
    infoService.delete(id);
    return OK;
  }

  public String delete(@NonNull List<String> ids){
    ids.forEach(this::delete);
    return OK;
  }

  String deleteByParentId(@NonNull String parentId) {
    val ids = repository.findByParentId(parentId);
    delete(ids);
    return OK;
  }

  public String findByBusinessKey(@NonNull String study, @NonNull String submitterId) {
    return repository.findByBusinessKey(study, submitterId);
  }

  public boolean isSampleExist(@NonNull String id){
    return !isNull(repository.read(id));
  }

  public void checkSampleExists(@NonNull String id){
    checkServer(isSampleExist(id), this.getClass(), SAMPLE_DOES_NOT_EXIST,
        "The sample with sampleId '%s' does not exist", id);
  }

  public void checkSampleDoesNotExist(@NonNull String id){
    checkServer(!isSampleExist(id), getClass(), SAMPLE_ALREADY_EXISTS,
        "The sample with sampleId '%s' already exists", id);
  }



}
