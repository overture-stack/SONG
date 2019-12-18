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

import static java.util.Objects.isNull;

import bio.overture.song.server.model.entity.Sample;
import bio.overture.song.server.model.entity.composites.CompositeEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class CompositeEntityService {

  @Autowired private final SampleService sampleService;

  @Autowired private final SpecimenService specimenService;

  @Autowired private final DonorService donorService;

  private static Sample buildPersistentSample(CompositeEntity s) {
    val out = new Sample();
    out.setWithSample(s);
    return out;
  }

  /**
   * The mutable CompositeEntity is really bad practice and needs to be refactored. Having any sort
   * of mutation of method arguments makes testing very difficult and allows for bugs to creep in
   * easier.
   */
  public String save(String studyId, CompositeEntity s) {
    String id = sampleService.findByBusinessKey(studyId, s.getSubmitterSampleId());
    if (isNull(id)) {
      val sampleCreateRequest = buildPersistentSample(s);
      s.setSpecimenId(getSampleParent(studyId, s));
      sampleCreateRequest.setSpecimenId(s.getSpecimenId());
      id = sampleService.create(studyId, sampleCreateRequest);
      s.setSampleId(id);
    } else {
      s.setSampleId(id);
      sampleService.update(s);
    }
    return id;
  }

  private String getSampleParent(String studyId, CompositeEntity s) {
    val specimen = s.getSpecimen();
    String id = specimenService.findByBusinessKey(studyId, specimen.getSubmitterSpecimenId());
    specimen.setDonorId(getSpecimenParent(studyId, s));
    if (isNull(id)) {
      id = specimenService.create(studyId, specimen);
    } else {
      specimen.setSpecimenId(id);
      specimenService.update(specimen);
    }
    return id;
  }

  private String getSpecimenParent(String studyId, CompositeEntity s) {
    return donorService.save(studyId, s.getDonor());
  }

  public CompositeEntity read(String sampleId) {
    val sample = CompositeEntity.create(sampleService.unsecuredRead(sampleId));
    sample.setSpecimen(specimenService.unsecuredRead(sample.getSpecimenId()));
    sample.setDonor(donorService.unsecuredRead(sample.getSpecimen().getDonorId()));
    return sample;
  }
}
