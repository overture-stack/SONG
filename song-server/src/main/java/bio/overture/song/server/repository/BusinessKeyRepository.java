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

package bio.overture.song.server.repository;

import bio.overture.song.server.model.entity.BusinessKeyView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusinessKeyRepository extends JpaRepository<BusinessKeyView, String> {

  List<BusinessKeyView> findAllByStudyIdAndSpecimenSubmitterId(String studyId, String specimenSubmitterId);
  List<BusinessKeyView> findAllByStudyIdAndSampleSubmitterId(String studyId, String sampleSubmitterId);
  long countAllByStudyIdAndSpecimenId(String studyId, String specimenId);
  long countAllByStudyIdAndSampleId(String studyId, String sampleId);
  Optional<BusinessKeyView> findBySpecimenId(String specimenId);
  Optional<BusinessKeyView> findBySampleId(String sampleId);

}
