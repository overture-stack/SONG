package org.icgc.dcc.sodalite.server.service;

import java.util.List;

import org.icgc.dcc.sodalite.server.model.Donor;
import org.icgc.dcc.sodalite.server.repository.DonorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class DonorService {

  @Autowired
  DonorRepository repository;
  @Autowired
  IdService idService;

  public String create(Donor d) {
    val donorId = idService.generateDonorId();
    d.setDonorId(donorId);
    val status = repository.save(donorId, d.getStudyId(), d.getDonorSubmitterId(), d.getDonorGender().toString());
    if (status != 1) {
      return "error: Can't create" + d.toString();
    }
    d.propagateKeys();

    return donorId;
  }

  public void update(Donor d) {
    if (repository.update(d.getDonorId(), d.getStudyId(), d.getDonorSubmitterId(),
        d.getDonorGender().toString()) == 1) {
      // in case any of the parent id's were changed
      d.propagateKeys();
    } else {
      throw new DatabaseRepositoryException(String.format("", d));
    }
  }

  public void delete(String id) {
    log.info(String.format("About to delete Donor with id %s", id));
    repository.delete(id);
  }

  /**
   * Are we sure we want to provide this capability?
   * 
   * @param studyId
   */
  public void deleteByParentId(String studyId) {
    log.info(String.format("About to delete all Donors belonging to Study %s", studyId));
    repository.getIds(studyId).forEach(id -> delete(id));
  }

  public Donor getById(String id) {
    val donor = repository.getById(id);
    if (donor == null) {
      return null;
    }
    return donor;
  }

  public Donor findByBusinessKey(String studyId, String submitterId) {
    return repository.getByBusinessKey(studyId, submitterId);
  }

  public List<Donor> findByParentId(String parentId) {
    val donors = repository.findByParentId(parentId);
    return donors;
  }

}