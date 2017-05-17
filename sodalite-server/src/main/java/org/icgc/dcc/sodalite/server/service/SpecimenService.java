package org.icgc.dcc.sodalite.server.service;

import java.util.List;

import org.icgc.dcc.sodalite.server.model.Specimen;
import org.icgc.dcc.sodalite.server.repository.SpecimenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.NoArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@NoArgsConstructor
public class SpecimenService {

  @Autowired
  IdService idService;
  @Autowired
  SpecimenRepository repository;
  @Autowired
  SampleService sampleService;

  public String create(Specimen specimen) {
    val specimenId = idService.generateSpecimenId();
    // TODO: modifying input parameter.....
    specimen.setSpecimenId(specimenId);
    int status =
        repository.save(specimenId, specimen.getStudyId(), specimen.getDonorId(), specimen.getSpecimenSubmitterId(), specimen.getSpecimenClass().toString(),
            specimen.getSpecimenType().toString());
    if (status != 1) {
      return "error: Can't create" + specimen.toString();
    }
    return specimenId;
  }

  public void update(Specimen s) {
    repository.update(s.getSpecimenId(), s.getStudyId(), s.getDonorId(), s.getSpecimenSubmitterId(), s.getSpecimenClass().toString(),
        s.getSpecimenType().toString());
  }

  public void delete(String id) {
    sampleService.deleteByParentId(id);
    log.info(String.format("About to delete Specimen with id %s", id));
    repository.delete(id);
  }

  public void deleteByParentId(String donorId) {
    log.info(String.format("About to delete all Specimens belonging to Donor %s", donorId));
    repository.getIds(donorId).forEach(this::delete);
  }

  public Specimen getById(String id) {
    val specimen = repository.getById(id);
    if (specimen == null) {
      return null;
    }
    return specimen;
  }

  public Specimen findByBusinessKey(String studyId, String submitterId) {
    return repository.getByBusinessKey(studyId, submitterId);
  }

  public List<Specimen> findByParentId(String donorId) {
    val specimens = repository.findByParentId(donorId);
    return specimens;
  }

}
