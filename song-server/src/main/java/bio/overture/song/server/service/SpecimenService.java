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
import static bio.overture.song.core.exceptions.ServerErrors.SPECIMEN_ALREADY_EXISTS;
import static bio.overture.song.core.exceptions.ServerErrors.SPECIMEN_DOES_NOT_EXIST;
import static bio.overture.song.core.exceptions.ServerErrors.SPECIMEN_ID_IS_CORRUPTED;
import static bio.overture.song.core.exceptions.ServerException.buildServerException;
import static bio.overture.song.core.exceptions.ServerException.checkServer;
import static bio.overture.song.core.utils.Responses.OK;
import static com.google.common.base.Strings.isNullOrEmpty;

import bio.overture.song.server.model.entity.BusinessKeyView;
import bio.overture.song.server.model.entity.Specimen;
import bio.overture.song.server.model.entity.composites.SpecimenWithSamples;
import bio.overture.song.server.repository.BusinessKeyRepository;
import bio.overture.song.server.repository.SpecimenRepository;
import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SpecimenService {

  @Autowired private final IdService idService;
  @Autowired private final SampleService sampleService;
  @Autowired private final SpecimenInfoService infoService;
  @Autowired private final SpecimenRepository repository;
  @Autowired private final StudyService studyService;
  @Autowired private final BusinessKeyRepository businessKeyRepository;

  private String createSpecimenId(String studyId, Specimen specimen) {
    studyService.checkStudyExist(studyId);
    val inputSpecimenId = specimen.getSpecimenId();
    val id = idService.generateSpecimenId(specimen.getSpecimenSubmitterId(), studyId);
    checkServer(
        isNullOrEmpty(inputSpecimenId) || id.equals(inputSpecimenId),
        getClass(),
        SPECIMEN_ID_IS_CORRUPTED,
        "The input specimenId '%s' is corrupted because it does not match the idServices specimenId '%s'",
        inputSpecimenId,
        id);
    checkSpecimenDoesNotExist(id);
    return id;
  }

  public String create(@NonNull String studyId, @NonNull Specimen specimen) {
    val id = createSpecimenId(studyId, specimen);
    specimen.setSpecimenId(id);
    repository.save(specimen);
    infoService.create(id, specimen.getInfoAsString());
    return id;
  }

  public Specimen securedRead(@NonNull String studyId, String id) {
    checkSpecimenRelatedToStudy(studyId, id);
    return unsecuredRead(id);
  }

  public Specimen unsecuredRead(@NonNull String id) {
    val specimenResult = repository.findById(id);
    checkServer(
        specimenResult.isPresent(),
        getClass(),
        SPECIMEN_DOES_NOT_EXIST,
        "The specimen for specimenId '%s' could not be read because it does not exist",
        id);
    val specimen = specimenResult.get();
    specimen.setInfo(infoService.readNullableInfo(id));
    return specimen;
  }

  public void checkSpecimenRelatedToStudy(@NonNull String studyId, @NonNull String id) {
    val numSpecimens = businessKeyRepository.countAllByStudyIdAndSpecimenId(studyId, id);
    if (numSpecimens < 1) {
      studyService.checkStudyExist(studyId);
      val specimen = unsecuredRead(id);
      val actualStudyId =
          businessKeyRepository.findBySpecimenId(id).map(BusinessKeyView::getStudyId).orElse(null);
      throw buildServerException(
          getClass(),
          ENTITY_NOT_RELATED_TO_STUDY,
          "The specimenId '%s' is not related to the input studyId '%s'. It is actually related to studyId '%s' and donorId '%s'",
          id,
          studyId,
          actualStudyId,
          specimen.getDonorId());
    }
  }

  SpecimenWithSamples readWithSamples(String id) {
    val specimen = unsecuredRead(id);
    val s = new SpecimenWithSamples();
    s.setSpecimen(specimen);
    s.setSamples(sampleService.readByParentId(id));
    return s;
  }

  List<SpecimenWithSamples> readByParentId(String parentId) {
    val donors = repository.findAllByDonorId(parentId);
    val specimens = new ArrayList<SpecimenWithSamples>();
    donors.forEach(d -> specimens.add(readWithSamples(d.getSpecimenId())));
    return specimens;
  }

  public String update(@NonNull Specimen specimenUpdate) {
    val specimen = unsecuredRead(specimenUpdate.getSpecimenId());

    specimen.setWithSpecimen(specimenUpdate);
    repository.save(specimenUpdate);
    infoService.update(specimenUpdate.getSpecimenId(), specimenUpdate.getInfoAsString());
    return OK;
  }

  public boolean isSpecimenExist(@NonNull String id) {
    return repository.existsById(id);
  }

  public void checkSpecimenExist(String id) {
    checkServer(
        isSpecimenExist(id),
        getClass(),
        SPECIMEN_DOES_NOT_EXIST,
        "The specimen with specimenId '%s' does not exist",
        id);
  }

  public void checkSpecimenDoesNotExist(String id) {
    checkServer(
        !isSpecimenExist(id),
        getClass(),
        SPECIMEN_ALREADY_EXISTS,
        "The specimen with specimenId '%s' already exists",
        id);
  }

  @Transactional
  public String securedDelete(@NonNull String studyId, String id) {
    checkSpecimenRelatedToStudy(studyId, id);
    return unsecuredDelete(id);
  }

  @Transactional
  public String unsecuredDelete(@NonNull String id) {
    checkSpecimenExist(id);
    sampleService.deleteByParentId(id);
    repository.deleteById(id);
    infoService.delete(id);
    return OK;
  }

  @Transactional
  public String securedDelete(@NonNull String studyId, List<String> ids) {
    ids.forEach(x -> securedDelete(studyId, x));
    return OK;
  }

  @Transactional
  public String unsecuredDelete(@NonNull List<String> ids) {
    ids.forEach(this::unsecuredDelete);
    return OK;
  }

  @Transactional
  String deleteByParentId(@NonNull String parentId) {
    repository.findAllByDonorId(parentId).stream()
        .map(Specimen::getSpecimenId)
        .forEach(this::unsecuredDelete);
    return OK;
  }

  public String findByBusinessKey(@NonNull String studyId, @NonNull String submitterId) {
    studyService.checkStudyExist(studyId);
    return businessKeyRepository.findAllByStudyIdAndSpecimenSubmitterId(studyId, submitterId)
        .stream()
        .map(BusinessKeyView::getSpecimenId)
        .findFirst()
        .orElse(null);
  }
}
