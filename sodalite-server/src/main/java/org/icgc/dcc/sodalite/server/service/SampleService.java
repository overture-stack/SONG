package org.icgc.dcc.sodalite.server.service;

import java.util.List;

import org.icgc.dcc.sodalite.server.model.Sample;
import org.icgc.dcc.sodalite.server.repository.SampleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.NoArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@NoArgsConstructor
public class SampleService {

  @Autowired
  SampleRepository repository;
  @Autowired
  IdService idService;
  @Autowired
  FileService fileService;

  public String create(Sample s) {
    val id = idService.generateSampleId();
    // TODO: modifying input parameter.....really just for error message if status != 1
    s.setSampleId(id);
    val status =
        repository.save(id, s.getStudyId(), s.getSpecimenId(), s.getSampleSubmitterId(), s.getSampleType().toString());

    if (status != 1) {
      return "error: Can't create" + s.toString();
    }

    return id;
  }

  public void update(Sample s) {
    repository.update(s.getSampleId(), s.getStudyId(), s.getSampleSubmitterId(), s.getSampleType().toString());
  }

  public void delete(String id) {
    fileService.deleteByParentId(id);
    log.info(String.format("About to delete Sample with id %s", id));
    repository.delete(id);
  }

  public void deleteByParentId(String parentId) {
    val ids = repository.getIds(parentId);
    log.info(String.format("About to delete all Samples belonging to Specimen %s", parentId));
    ids.forEach(this::delete);
  }

  public Sample getById(String id) {
    val sample = repository.getById(id);
    if (sample == null) {
      return null;
    }

    return sample;
  }

  public Sample findByBusinessKey(String studyId, String submitterId) {
    return repository.getByBusinessKey(studyId, submitterId);
  }

  public List<Sample> findByParentId(String parentId) {
    val samples = repository.findByParentId(parentId);
    return samples;
  }

}
