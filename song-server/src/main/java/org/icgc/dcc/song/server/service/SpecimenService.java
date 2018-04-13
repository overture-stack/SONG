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
import lombok.val;
import org.icgc.dcc.song.server.model.entity.Donor;
import org.icgc.dcc.song.server.model.entity.Specimen;
import org.icgc.dcc.song.server.model.entity.composites.SpecimenWithSamples;
import org.icgc.dcc.song.server.repository.DonorRepository;
import org.icgc.dcc.song.server.repository.SpecimenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Objects.isNull;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SPECIMEN_ALREADY_EXISTS;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SPECIMEN_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SPECIMEN_ID_IS_CORRUPTED;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SPECIMEN_RECORD_FAILED;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SPECIMEN_REPOSITORY_DELETE_RECORD;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SPECIMEN_REPOSITORY_UPDATE_RECORD;
import static org.icgc.dcc.song.core.exceptions.ServerException.checkServer;
import static org.icgc.dcc.song.core.utils.Responses.OK;

@RequiredArgsConstructor
@Service
public class SpecimenService {

  @Autowired
  private final IdService idService;
  @Autowired
  private final SampleService sampleService;
  @Autowired
  private final SpecimenInfoService infoService;
  @Autowired
  private final SpecimenRepository repository;
  @Autowired
  private final StudyService studyService;

  private String createSpecimenId(String studyId, Specimen specimen){
    studyService.checkStudyExist(studyId);
    val inputSpecimenId = specimen.getSpecimenId();
    val id = idService.generateSpecimenId(specimen.getSpecimenSubmitterId(), studyId);
    checkServer(isNullOrEmpty(inputSpecimenId) || id.equals(inputSpecimenId), getClass(),
        SPECIMEN_ID_IS_CORRUPTED,
        "The input specimenId '%s' is corrupted because it does not match the idServices specimenId '%s'",
        inputSpecimenId, id);
    checkSpecimenDoesNotExist(id);
    return id;
  }

  public String create(@NonNull String studyId, @NonNull Specimen specimen) {
    val id = createSpecimenId(studyId, specimen);
    specimen.setSpecimenId(id);
    int status = repository.create(specimen);
    checkServer(status == 1, this.getClass(), SPECIMEN_RECORD_FAILED,
          "Cannot create Specimen: %s", specimen.toString());
    infoService.create(id, specimen.getInfoAsString());
    return id;
  }

  public Specimen read(@NonNull String id) {
    val specimen = repository.read(id);
    checkServer(!isNull(specimen), getClass(), SPECIMEN_DOES_NOT_EXIST,
        "The specimen for specimenId '%s' could not be read because it does not exist", id);
    specimen.setInfo(infoService.readNullableInfo(id));
    return specimen;
  }

  SpecimenWithSamples readWithSamples(String id) {
    val specimen = read(id);
    val s = new SpecimenWithSamples();
    s.setSpecimen(specimen);
    s.setSamples(sampleService.readByParentId(id));
    return s;
  }

  List<SpecimenWithSamples> readByParentId(String parentId) {
    val ids = repository.findByParentId(parentId);
    val specimens = new ArrayList<SpecimenWithSamples>();
    ids.forEach(id -> specimens.add(readWithSamples(id)));
    return specimens;
  }

  public String update(@NonNull Specimen specimen) {
    checkSpecimenExist(specimen.getSpecimenId());
    val status = repository.update(specimen);
    checkServer(status == 1, getClass(), SPECIMEN_REPOSITORY_UPDATE_RECORD,
        "Cannot update specimenId '%s' for specimen '%s'", specimen.getSpecimenId(), specimen);
    infoService.update(specimen.getSpecimenId(), specimen.getInfoAsString());
    return OK;
  }

  public boolean isSpecimenExist(@NonNull String id){
    return !isNull(repository.read(id));
  }

  public void checkSpecimenExist(String id){
    checkServer(isSpecimenExist(id), getClass(), SPECIMEN_DOES_NOT_EXIST,
        "The specimen with specimenId '%s' does not exist", id);
  }

  public void checkSpecimenDoesNotExist(String id){
    checkServer(!isSpecimenExist(id), getClass(), SPECIMEN_ALREADY_EXISTS,
        "The specimen with specimenId '%s' already exists", id);
  }

  public String delete(@NonNull String id) {
    checkSpecimenExist(id);
    sampleService.deleteByParentId(id);
    val status  = repository.delete(id);
    checkServer(status == 1, getClass(), SPECIMEN_REPOSITORY_DELETE_RECORD,
        "Cannot delete specimen with specimenId '%s'", id);
    infoService.delete(id);
    return OK;
  }

  public String delete(@NonNull List<String> ids) {
    ids.forEach(this::delete);
    return OK;
  }

  String deleteByParentId(@NonNull String parentId) {
    repository.findByParentId(parentId).forEach(this::delete);
    return OK;
  }

  @Autowired DonorRepository donorRepository;
  public String findByBusinessKey(@NonNull String studyId, @NonNull String submitterId) {
    studyService.checkStudyExist(studyId);

    Donor.create(null, )
    donorRepository.

    return repository.findByBusinessKey(studyId, submitterId);
  }


}
