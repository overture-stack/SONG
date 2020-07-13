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

import static bio.overture.song.core.exceptions.ServerErrors.MISMATCHING_SAMPLE_DATA;
import static bio.overture.song.core.exceptions.ServerErrors.MISMATCHING_SPECIMEN_DATA;
import static bio.overture.song.core.exceptions.ServerErrors.SAMPLE_TO_SPECIMEN_ID_MISMATCH;
import static bio.overture.song.core.exceptions.ServerErrors.SPECIMEN_TO_DONOR_ID_MISMATCH;
import static bio.overture.song.core.exceptions.ServerException.checkServer;
import static java.util.Objects.isNull;

import bio.overture.song.server.model.entity.Sample;
import bio.overture.song.server.model.entity.Specimen;
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
    val submitterSampleId = s.getSubmitterSampleId();

    val submitterSpecimenId = s.getSpecimen().getSubmitterSpecimenId();
    String id = sampleService.findByBusinessKey(studyId, submitterSampleId);

    s.setSpecimenId(getSampleParent(studyId, s));

    if (isNull(id)) {
      val sampleCreateRequest = buildPersistentSample(s);

      sampleCreateRequest.setSpecimenId(s.getSpecimenId());
      id = sampleService.create(studyId, sampleCreateRequest);
      s.setSampleId(id);
    } else {
      val sample = sampleService.securedRead(studyId, id);
      val specimen = specimenService.securedRead(studyId, sample.getSpecimenId());

      checkServer(
          specimen.getSubmitterSpecimenId().equals(submitterSpecimenId),
          getClass(),
          SAMPLE_TO_SPECIMEN_ID_MISMATCH,
          "Existing sample (submitterSampleId='%s') has submitterSpecimenId='%s', but this submission says it has "
              + "submitterSpecimenId='%s' instead. Please re-submit with the correct submitterSpecimenId.",
          submitterSampleId,
          specimen.getSubmitterSpecimenId(),
          submitterSpecimenId);
      s.setSampleId(id);
      checkSameSample(sample, s);
    }
    return id;
  }

  private void checkSameSample(Sample existing, CompositeEntity input) {
    val newSample = new Sample();
    newSample.setWithSample(input);
    checkServer(
        existing.equals(newSample),
        getClass(),
        MISMATCHING_SAMPLE_DATA,
        "Input Sample data does not match the existing Sample data for submitterSampleId '%s'. Ensure the data matches.",
        existing.getSubmitterSampleId());
  }

  private String getSampleParent(String studyId, CompositeEntity s) {
    val inputSpecimen = s.getSpecimen();
    String specimenId =
        specimenService.findByBusinessKey(studyId, inputSpecimen.getSubmitterSpecimenId());

    if (isNull(specimenId)) {
      inputSpecimen.setDonorId(getSpecimenParent(studyId, s));
      specimenId = specimenService.create(studyId, inputSpecimen);
    } else {
      val existingSpecimen = specimenService.securedRead(studyId, specimenId);
      val existingDonor = donorService.securedRead(studyId, existingSpecimen.getDonorId());
      checkServer(
          s.getDonor().getSubmitterDonorId().equals(existingDonor.getSubmitterDonorId()),
          getClass(),
          SPECIMEN_TO_DONOR_ID_MISMATCH,
          "Existing specimen (specimenId='%s') donorId='%s', but this submission says it has "
              + "donorId='%s' instead. Please re-submit with the correct donorId.",
          existingSpecimen.getSubmitterSpecimenId(),
          existingDonor.getSubmitterDonorId(),
          s.getDonor().getSubmitterDonorId());
      inputSpecimen.setSpecimenId(specimenId);
      inputSpecimen.setDonorId(getSpecimenParent(studyId, s));
      checkSameSpecimen(existingSpecimen, inputSpecimen);
    }
    return specimenId;
  }

  private void checkSameSpecimen(Specimen existing, Specimen input) {
    checkServer(
        existing.equals(input),
        getClass(),
        MISMATCHING_SPECIMEN_DATA,
        "Input Specimen data does not match the existing Specimen data for submitterSpecimenId '%s'. Ensure the data matches.",
        existing.getSubmitterSpecimenId());
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
