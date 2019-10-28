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

import bio.overture.song.server.model.entity.composites.StudyWithDonors;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudyWithDonorsService {

  @Autowired private StudyService studyService;

  @Autowired private DonorService donorService;

  @SneakyThrows
  public StudyWithDonors readWithChildren(String studyId) {
    val study = new StudyWithDonors();
    val s = studyService.read(studyId);

    study.setStudy(s);
    study.setDonors(donorService.readByParentId(studyId));
    return study;
  }
}
