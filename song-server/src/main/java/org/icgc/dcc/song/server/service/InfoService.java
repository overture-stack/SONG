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
import org.icgc.dcc.song.server.model.enums.InfoTypes;
import org.icgc.dcc.song.server.repository.InfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static java.util.Objects.isNull;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.INFO_ALREADY_EXISTS;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.INFO_NOT_FOUND;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.INFO_REPOSITORY_CREATE_RECORD;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.INFO_REPOSITORY_DELETE_RECORD;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.INFO_REPOSITORY_UPDATE_RECORD;
import static org.icgc.dcc.song.core.exceptions.ServerException.checkServer;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.ANALYSIS;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.DONOR;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.FILE;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.SAMPLE;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.SEQUENCING_READ;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.SPECIMEN;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.STUDY;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.VARIANT_CALL;

@Service
@RequiredArgsConstructor
abstract class InfoService {

  private final InfoTypes type;
  private final InfoRepository infoRepository;

  public Optional<String> readInfo(@NonNull String id) {
    checkInfoExists(id); //TODO: optimize by returning Info entity, so that only 1 db read is done instead of 2.
    return Optional.ofNullable(infoRepository.readInfo(id, type.toString()));
  }

  public String readNullableInfo(String id) {
    return infoRepository.readInfo(id, type.toString());
  }

  public void checkInfoExists(@NonNull String id){
    checkServer(isInfoExist(id), getClass(), INFO_NOT_FOUND,
        "The Info record for id='%s' and type='%s' was not found", id, type.toString());
  }

  /**
   * Using readType method since readInfo can return null
   */
  public boolean isInfoExist(@NonNull String id){
    return !isNull(infoRepository.readType(id, type.toString()));
  }

  public void create( @NonNull String id,  String info) {
    checkServer(!isInfoExist(id),getClass(), INFO_ALREADY_EXISTS,
    "Could not create Info record for id='%s' and type='%s' because it already exists",
    id, type.toString());
    val status = infoRepository.create(id, type.toString(), info);
    checkServer(status == 1, getClass(), INFO_REPOSITORY_CREATE_RECORD,
        "Could not create Info record for id='%s' and type='%s' in repository", id, type.toString());
  }

  public void update(@NonNull String id, String info) {
    checkInfoExists(id);
    val status = infoRepository.set(id, type.toString(), info);
    checkServer(status == 1, getClass(), INFO_REPOSITORY_UPDATE_RECORD,
        "Could not update Info record for id='%s' and type='%s' in repository", id, type.toString());
  }

  public void delete(@NonNull String id) {
    checkInfoExists(id);
    val status = infoRepository.delete(id, type.toString());
    checkServer(status ==1, getClass(), INFO_REPOSITORY_DELETE_RECORD,
          "Could not delete Info record for id='%s' and type='%s' in repository", id, type.toString());
  }

}

@Service
class StudyInfoService extends InfoService {
  @Autowired
  StudyInfoService(InfoRepository repo) {
    super(STUDY, repo);
  }
}

@Service
class DonorInfoService extends InfoService {
  @Autowired
  DonorInfoService(InfoRepository repo) {
    super(DONOR, repo);
  }
}

@Service
class SpecimenInfoService extends InfoService {
  @Autowired
  SpecimenInfoService(InfoRepository repo) {
    super(SPECIMEN, repo);
  }
}

@Service
class SampleInfoService extends InfoService {
  @Autowired
  SampleInfoService(InfoRepository repo) {
    super(SAMPLE, repo);
  }
}

@Service
class FileInfoService extends InfoService {
  @Autowired
  FileInfoService(InfoRepository repo) {
    super(FILE, repo);
  }
}

@Service
class AnalysisInfoService extends InfoService {
  @Autowired
  AnalysisInfoService(InfoRepository repo) {
    super(ANALYSIS, repo);
  }
}

@Service
class VariantCallInfoService extends InfoService {
  @Autowired
  VariantCallInfoService(InfoRepository repo) {
    super(VARIANT_CALL, repo);
  }
}

@Service
class SequencingReadInfoService extends InfoService {
  @Autowired
  SequencingReadInfoService(InfoRepository repo) {
    super(SEQUENCING_READ, repo);
  }
}

