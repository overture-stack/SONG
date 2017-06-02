package org.icgc.dcc.sodalite.server.service;

import static org.icgc.dcc.sodalite.server.model.enums.IdPrefix.Sample;

import java.util.List;

import org.icgc.dcc.sodalite.server.model.entity.Sample;
import org.icgc.dcc.sodalite.server.repository.SampleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.NoArgsConstructor;
import lombok.val;

@Service
@NoArgsConstructor
public class SampleService {

  @Autowired
  SampleRepository repository;
  @Autowired
  IdService idService;
  @Autowired
  FileService fileService;

  public String create(String parentId, Sample s) {
    val id = idService.generate(Sample);
    s.setSampleId(id);
    s.setSpecimenId(parentId);
    int status = repository.create(id, parentId, s.getSampleSubmitterId(), s.getSampleType().toString());

    if (status != 1) {
      return "error: Can't create" + s.toString();
    }
    s.getFiles().forEach(f -> fileService.create(id, f));

    return "ok:" + id;
  }

  public String update(Sample s) {
    repository.update(s.getSampleId(), s.getSampleSubmitterId(), s.getSampleType().toString());
    return "ok";
  }

  public String delete(String id) {
    repository.delete(id);

    return "ok";
  }

  public String deleteByParentId(String parentId) {
    val ids = repository.findByParentId(parentId);
    ids.forEach(this::delete);

    return "ok";
  }

  public Sample getById(String id) {
    val sample = repository.read(id);
    if (sample == null) {
      return null;
    }
    sample.setFiles(fileService.findByParentId(id));
    return sample;
  }

  public List<Sample> findByParentId(String parentId) {
    val samples = repository.readByParentId(parentId);
    samples.forEach(s -> s.setFiles(fileService.findByParentId(s.getSampleId())));
    return samples;
  }

}
