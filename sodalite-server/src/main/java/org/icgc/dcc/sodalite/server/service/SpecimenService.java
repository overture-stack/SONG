package org.icgc.dcc.sodalite.server.service;

import static org.icgc.dcc.sodalite.server.model.enums.IdPrefix.Specimen;

import java.util.List;

import org.icgc.dcc.sodalite.server.model.entity.Specimen;
import org.icgc.dcc.sodalite.server.model.enums.IdPrefix;
import org.icgc.dcc.sodalite.server.repository.SpecimenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
@Service
public class SpecimenService {

  @Autowired
  IdService idService;
  @Autowired
  SpecimenRepository repository;
  @Autowired
  SampleService sampleService;

  public String create(String parentId, Specimen specimen) {
    val id = idService.generate(Specimen);
    specimen.setSpecimenId(id);
    specimen.setDonorId(parentId);
    int status =
        repository.create(specimen);
    if (status != 1) {
      return "error: Can't create" + specimen.toString();
    }
    specimen.getSamples().forEach(s -> sampleService.create(id, s));
    return "ok:" + id;
  }

  public Specimen read(String id) {
    val specimen = repository.read(id);
    if (specimen == null) {
      return null;
    }
    specimen.setSamples(sampleService.readByParentId(id));
    return specimen;
  }

  public List<Specimen> readByParentId(String parentId) {
    val specimens = repository.readByParentId(parentId);
    specimens.forEach(s -> s.setSamples(sampleService.readByParentId(s.getSpecimenId())));
    return specimens;
  }

  public String update(Specimen s) {
    repository.update(s);
    return "ok";
  }

  public String delete(String id) {
    sampleService.deleteByParentId(id);
    repository.delete(id);
    return "ok";
  }

  public String deleteByParentId(String parentId) {
    repository.findByParentId(parentId).forEach(this::delete);
    return "ok";
  }

  public List<String> findByParentId(String donorId) {
    return repository.findByParentId(donorId);
  }

  public String findByBusinessKey(String studyId, String submitterId) {
    return repository.findByBusinessKey(studyId, submitterId);
  }

  public String save(String studyId, Specimen specimen) {
    String specimenId = repository.findByBusinessKey(studyId, specimen.getSpecimenSubmitterId());
    if (specimenId == null) {
      specimenId = idService.generate(IdPrefix.Specimen);
      specimen.setSpecimenId(specimenId);
      repository.create(specimen);
    } else {
      specimen.setSpecimenId(specimenId);
      repository.update(specimen);
    }
    return specimenId;
  }

}
