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
package bio.overture.song.server.service;

import static bio.overture.song.core.exceptions.ServerErrors.STUDY_ALREADY_EXISTS;
import static bio.overture.song.core.exceptions.ServerErrors.STUDY_ID_DOES_NOT_EXIST;
import static bio.overture.song.core.exceptions.ServerException.checkServer;
import static java.lang.Thread.currentThread;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;

import bio.overture.song.server.model.entity.Study;
import bio.overture.song.server.repository.StudyRepository;
import java.util.List;
import javax.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudyService {

  @Autowired StudyRepository studyRepository;

  @Autowired StudyInfoService infoService;

  @SneakyThrows
  public Study read(String studyId) {
    val studyResult = studyRepository.findById(studyId);
    checkServer(
        studyResult.isPresent(),
        getClass(),
        STUDY_ID_DOES_NOT_EXIST,
        "The studyId '%s' does not exist",
        studyId);
    val study = studyResult.get();
    val info = infoService.readNullableInfo(studyId);
    study.setInfo(info);
    return study;
  }

  public boolean isStudyExist(String studyId) {
    return studyRepository.existsById(studyId);
  }

  @Transactional
  public String saveStudy(Study study) {
    val id = study.getStudyId();
    checkServer(
        !isStudyExist(id),
        getClass(),
        STUDY_ALREADY_EXISTS,
        "The studyId '%s' already exists. Cannot save the study: %s ",
        id,
        study);
    studyRepository.save(study);
    infoService.create(id, study.getInfoAsString());
    return id;
  }

  public List<String> findAllStudies() {
    return studyRepository.findAll().stream().map(Study::getStudyId).collect(toImmutableList());
  }

  @SneakyThrows
  public void checkStudyExist(@NonNull String studyId) {
    val previousCallingClass = Class.forName(currentThread().getStackTrace()[2].getClassName());
    checkServer(
        isStudyExist(studyId),
        previousCallingClass,
        STUDY_ID_DOES_NOT_EXIST,
        "The studyId '%s' does not exist",
        studyId);
  }
}
