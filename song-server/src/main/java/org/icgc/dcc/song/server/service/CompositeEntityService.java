package org.icgc.dcc.song.server.service;

import lombok.AllArgsConstructor;

import lombok.val;

import org.icgc.dcc.song.server.model.entity.composites.CompositeEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static java.util.Objects.isNull;

@Service
@AllArgsConstructor
public class CompositeEntityService {

  @Autowired
  private final SampleService sampleService;

  @Autowired
  private final SpecimenService specimenService;

  @Autowired
  private final DonorService donorService;

  public String save(String studyId, CompositeEntity s) {
    String id = sampleService.findByBusinessKey(studyId, s.getSampleSubmitterId());
    if (isNull(id)) {
      s.setSpecimenId(getSampleParent(studyId, s));
      id = sampleService.create(studyId, s);
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
      specimenService.update(specimen);
    }
    return id;
  }

  private String getSpecimenParent(String studyId, CompositeEntity s) {
    return donorService.save(studyId, s.getDonor());
  }

  public CompositeEntity read(String sampleId) {
    val sample = CompositeEntity.create(sampleService.read(sampleId));
    sample.setSpecimen(specimenService.read(sample.getSpecimenId()));
    sample.setDonor(donorService.read(sample.getSpecimen().getDonorId()));
    return sample;
  }

}
