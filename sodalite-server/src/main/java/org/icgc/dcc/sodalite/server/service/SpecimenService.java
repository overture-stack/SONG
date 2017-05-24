package org.icgc.dcc.sodalite.server.service;

import java.util.List;

import org.icgc.dcc.sodalite.server.model.entity.Specimen;
import org.icgc.dcc.sodalite.server.repository.SpecimenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.NoArgsConstructor;
import lombok.val;

@Service
@NoArgsConstructor
public class SpecimenService {

  @Autowired
  IdService idService;
  @Autowired
  SpecimenRepository repository;
  @Autowired
  SampleService sampleService;

  public String create(String parentId, Specimen specimen) {
    val id = idService.generateSpecimenId();
    specimen.setSpecimenId(id);
    int status =
        repository.save(id, parentId, specimen.getSpecimenSubmitterId(), specimen.getSpecimenClass().toString(),
            specimen.getSpecimenType().toString());
    if (status != 1) {
      return "error: Can't create" + specimen.toString();
    }
    specimen.getSamples().forEach(s -> sampleService.create(id, s));
    return "ok:" + id;
  }

  public String update(Specimen s) {
    repository.set(s.getSpecimenId(), s.getSpecimenSubmitterId(), s.getSpecimenClass().toString(),
        s.getSpecimenType().toString());
    return "ok";
  }

  public String delete(String id) {
    sampleService.deleteByParentId(id);
    repository.delete(id);
    return "ok";
  }

  public String deleteByParentId(String parentId) {
    repository.getIds(parentId).forEach(this::delete);
    return "ok";
  }

  public Specimen getById(String id) {
    val specimen = repository.getById(id);
    if (specimen == null) {
      return null;
    }
    specimen.setSamples(sampleService.findByParentId(id));
    return specimen;
  }

  public List<Specimen> findByParentId(String parentId) {
    val specimens = repository.findByParentId(parentId);
    specimens.forEach(s -> s.setSamples(sampleService.findByParentId(s.getSpecimenId())));
    return specimens;
  }

}
