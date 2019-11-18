/*
 * Copyright (c) 2019. Ontario Institute for Cancer Research
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
package bio.overture.song.server.service;

import static bio.overture.song.core.exceptions.ServerErrors.ENTITY_NOT_RELATED_TO_STUDY;
import static bio.overture.song.core.exceptions.ServerErrors.ID_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerErrors.SAMPLE_ALREADY_EXISTS;
import static bio.overture.song.core.exceptions.ServerErrors.SAMPLE_DOES_NOT_EXIST;
import static bio.overture.song.core.exceptions.ServerErrors.SAMPLE_ID_IS_CORRUPTED;
import static bio.overture.song.core.exceptions.ServerException.buildServerException;
import static bio.overture.song.core.exceptions.ServerException.checkServer;
import static bio.overture.song.core.exceptions.ServerException.checkServerOptional;
import static bio.overture.song.core.utils.Responses.OK;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.toList;

import bio.overture.song.server.model.entity.BusinessKeyView;
import bio.overture.song.server.model.entity.Sample;
import bio.overture.song.server.repository.BusinessKeyRepository;
import bio.overture.song.server.repository.SampleRepository;
import bio.overture.song.server.service.id.IdService;
import java.util.List;
import javax.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class SampleService {

  @Autowired private final SampleRepository repository;

  @Autowired private final SampleInfoService infoService;

  @Autowired private final IdService idService;

  @Autowired private final StudyService studyService;

  @Autowired private final BusinessKeyRepository businessKeyRepository;

  // TODO: [Related to SONG-260] should we add a
  // specimenService.checkSpecimenExists(sample.getSpecimenId()) here?
  public String create(@NonNull String studyId, @NonNull Sample sample) {
    val id = createSampleId(studyId, sample);
    sample.setSampleId(id);
    sample.setSpecimenId(sample.getSpecimenId());
    repository.save(sample);
    infoService.create(id, sample.getInfoAsString());
    return id;
  }

  public void checkSampleRelatedToStudy(@NonNull String studyId, @NonNull String id) {
    val numSamples = businessKeyRepository.countAllByStudyIdAndSampleId(studyId, id);
    if (numSamples < 1) {
      studyService.checkStudyExist(studyId);
      val sample = unsecuredRead(id);
      val actualStudyId =
          businessKeyRepository.findBySampleId(id).map(BusinessKeyView::getStudyId).orElse(null);
      throw buildServerException(
          getClass(),
          ENTITY_NOT_RELATED_TO_STUDY,
          "The sampleId '%s' is not related to the input studyId '%s'. It is actually related to studyId '%s' and specimenId '%s'",
          id,
          studyId,
          actualStudyId,
          sample.getSpecimenId());
    }
  }

  public Sample securedRead(@NonNull String studyId, String id) {
    checkSampleRelatedToStudy(studyId, id);
    return unsecuredRead(id);
  }

  public String findByBusinessKey(@NonNull String study, @NonNull String submitterId) {
    return businessKeyRepository.findAllByStudyIdAndSampleSubmitterId(study, submitterId).stream()
        .map(BusinessKeyView::getSampleId)
        .findFirst()
        .orElse(null);
  }

  public boolean isSampleExist(@NonNull String id) {
    return repository.existsById(id);
  }

  public void checkSampleExists(@NonNull String id) {
    checkServer(
        isSampleExist(id),
        this.getClass(),
        SAMPLE_DOES_NOT_EXIST,
        "The sample with sampleId '%s' does not exist",
        id);
  }

  public void checkSampleDoesNotExist(@NonNull String id) {
    checkServer(
        !isSampleExist(id),
        getClass(),
        SAMPLE_ALREADY_EXISTS,
        "The sample with sampleId '%s' already exists",
        id);
  }

  @Transactional
  public String securedDelete(String studyId, @NonNull List<String> ids) {
    ids.forEach(x -> securedDelete(studyId, x));
    return OK;
  }

  @Transactional
  public String securedDelete(@NonNull String studyId, @NonNull String id) {
    checkSampleRelatedToStudy(studyId, id);
    return unsecuredDelete(id);
  }

  Sample unsecuredRead(@NonNull String id) {
    val sampleResult = repository.findById(id);
    checkServer(
        sampleResult.isPresent(),
        getClass(),
        SAMPLE_DOES_NOT_EXIST,
        "The sample for sampleId '%s' could not be read because it does not exist",
        id);
    val sample = sampleResult.get();
    sample.setInfo(infoService.readNullableInfo(id));
    return sample;
  }

  List<Sample> readByParentId(@NonNull String parentId) {
    return repository.findAllBySpecimenId(parentId);
  }

  String update(@NonNull Sample sampleUpdate) {
    val sample = unsecuredRead(sampleUpdate.getSampleId());
    sample.setSampleSubmitterId(sampleUpdate.getSampleSubmitterId());
    sample.setSampleType(sampleUpdate.getSampleType());
    sample.setInfo(sampleUpdate.getInfo());
    repository.save(sample);
    infoService.update(sample.getSampleId(), sample.getInfoAsString());
    return OK;
  }

  String deleteByParentId(@NonNull String parentId) {
    val ids =
        repository.findAllBySpecimenId(parentId).stream()
            .map(Sample::getSampleId)
            .collect(toList());
    unsecuredDelete(ids);
    return OK;
  }

  private String createSampleId(String studyId, Sample sample) {
    studyService.checkStudyExist(studyId);
    val inputSampleId = sample.getSampleId();
    val sampleSubmitterId = sample.getSampleSubmitterId();
    val result = idService.getSampleId(studyId, sampleSubmitterId);
    val id =
        checkServerOptional(
            result,
            getClass(),
            ID_NOT_FOUND,
            "The sampleId for studyId '%s' and sampleSubmitterId '%s' was not found",
            studyId,
            sampleSubmitterId);
    checkServer(
        isNullOrEmpty(inputSampleId) || id.equals(inputSampleId),
        getClass(),
        SAMPLE_ID_IS_CORRUPTED,
        "The input sampleId '%s' is corrupted because it does not match the federated sampleId '%s'",
        inputSampleId,
        id);
    checkSampleDoesNotExist(id);
    return id;
  }

  private String unsecuredDelete(@NonNull String id) {
    checkSampleExists(id);
    repository.deleteById(id);
    infoService.delete(id);
    return OK;
  }

  private void unsecuredDelete(@NonNull List<String> ids) {
    ids.forEach(this::unsecuredDelete);
  }
}
