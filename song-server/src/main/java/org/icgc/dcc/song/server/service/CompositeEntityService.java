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

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.server.model.entity.Sample;
import org.icgc.dcc.song.server.model.entity.composites.CompositeEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static java.util.Objects.isNull;

@Service
@AllArgsConstructor
@Slf4j
public class CompositeEntityService {

  @Autowired
  private final SampleService sampleService;

  @Autowired
  private final SpecimenService specimenService;

  @Autowired
  private final DonorService donorService;

  private static Sample buildPersistentSample(CompositeEntity s){
    val out = new Sample();
    out.setWithSample(s);
    return out;
  }

  public String save(String studyId, CompositeEntity s) {
    String id = sampleService.findByBusinessKey(studyId, s.getSampleSubmitterId());
    s.setSampleId(id);
    if (isNull(id)) {
      val sampleCreateRequest = buildPersistentSample(s);
      s.setSpecimenId(getSampleParent(studyId, s));
      sampleCreateRequest.setSpecimenId(s.getSpecimenId());
      id = sampleService.create(studyId, sampleCreateRequest);
    } else {
      sampleService.update(s);
    }
    return id;
  }

  private String getSampleParent(String studyId, CompositeEntity s) {
    val specimen = s.getSpecimen();
    String id = specimenService.findByBusinessKey(studyId, specimen.getSpecimenSubmitterId());
    if (isNull(id)) {
      specimen.setDonorId(getSpecimenParent(studyId, s));
      id = specimenService.create(studyId, specimen);
    } else {
      s.setSpecimenId(id);
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
