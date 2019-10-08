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

import static bio.overture.song.core.exceptions.ServerErrors.INFO_ALREADY_EXISTS;
import static bio.overture.song.core.exceptions.ServerErrors.INFO_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerException.checkServer;
import static bio.overture.song.core.utils.JsonUtils.toJson;
import static bio.overture.song.core.utils.JsonUtils.toJsonNode;
import static bio.overture.song.server.model.entity.Info.createInfo;
import static bio.overture.song.server.model.entity.InfoPK.createInfoPK;
import static bio.overture.song.server.model.enums.InfoTypes.SEQUENCING_READ;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.server.model.entity.Info;
import bio.overture.song.server.model.entity.InfoPK;
import bio.overture.song.server.model.enums.InfoTypes;
import bio.overture.song.server.repository.InfoRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
abstract class InfoService {

  private final InfoTypes type;
  private final InfoRepository infoRepository;

  public Optional<String> readInfo(@NonNull String id) {
    checkInfoExists(
        id); // TODO: optimize by returning Info entity, so that only 1 db read is done instead of
    // 2.
    return unsafeRead(id);
  }

  public Map<String, String> getInfoMap(@NonNull Set<String> ids) {
    return findInfo(ids).stream()
        .collect(toMap(x -> x.getInfoPK().getId(), x -> toJson(toJsonNode(x.getInfo()))));
  }

  private Optional<String> unsafeRead(String id) {
    return infoRepository
        .findById(buildPK(id))
        .map(Info::getInfo)
        .map(JsonUtils::toJsonNode)
        .map(JsonUtils::toJson);
  }

  public String readNullableInfo(String id) {
    return unsafeRead(id).orElse(null);
  }

  private InfoPK buildPK(String id) {
    return createInfoPK(id, type.toString());
  }

  public void checkInfoExists(@NonNull String id) {
    checkServer(
        isInfoExist(id),
        getClass(),
        INFO_NOT_FOUND,
        "The Info record for id='%s' and type='%s' was not found",
        id,
        type.toString());
  }

  /** Using readType method since readInfo can return null */
  public boolean isInfoExist(@NonNull String id) {
    return infoRepository.existsById(buildPK(id));
  }

  public void create(@NonNull String id, String info) {
    checkServer(
        !isInfoExist(id),
        getClass(),
        INFO_ALREADY_EXISTS,
        "Could not create Info record for id='%s' and type='%s' because it already exists",
        id,
        type.toString());
    val infoObj = createInfo(buildPK(id), info);
    infoRepository.save(infoObj);
  }

  public void update(@NonNull String id, String info) {
    checkInfoExists(id);
    val infoObj = createInfo(buildPK(id), info);
    infoRepository.save(infoObj);
  }

  public void delete(@NonNull String id) {
    checkInfoExists(id);
    infoRepository.deleteById(buildPK(id));
  }

  private List<Info> findInfo(@NonNull Set<String> ids) {
    val searchRequest = ids.stream().map(this::buildPK).collect(toList());
    return infoRepository.findAllByInfoPKIn(searchRequest);
  }
}

@Service
class StudyInfoService extends InfoService {
  @Autowired
  StudyInfoService(InfoRepository repo) {
    super(InfoTypes.STUDY, repo);
  }
}

@Service
class DonorInfoService extends InfoService {
  @Autowired
  DonorInfoService(InfoRepository repo) {
    super(InfoTypes.DONOR, repo);
  }
}

@Service
class SpecimenInfoService extends InfoService {
  @Autowired
  SpecimenInfoService(InfoRepository repo) {
    super(InfoTypes.SPECIMEN, repo);
  }
}

@Service
class SampleInfoService extends InfoService {
  @Autowired
  SampleInfoService(InfoRepository repo) {
    super(InfoTypes.SAMPLE, repo);
  }
}

@Service
class FileInfoService extends InfoService {
  @Autowired
  FileInfoService(InfoRepository repo) {
    super(InfoTypes.FILE, repo);
  }
}

@Service
class VariantCallInfoService extends InfoService {
  @Autowired
  VariantCallInfoService(InfoRepository repo) {
    super(InfoTypes.VARIANT_CALL, repo);
  }
}

@Service
class SequencingReadInfoService extends InfoService {
  @Autowired
  SequencingReadInfoService(InfoRepository repo) {
    super(SEQUENCING_READ, repo);
  }
}
