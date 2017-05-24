package org.icgc.dcc.sodalite.server.service;

import java.util.List;

import org.icgc.dcc.sodalite.server.model.entity.Donor;
import org.icgc.dcc.sodalite.server.repository.DonorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
@Service
public class DonorService {

  @Autowired
  DonorRepository donorRepository;
  @Autowired
  IdService idService;
  @Autowired
  SpecimenService specimenService;

  public String create(String parentId, Donor d) {
    String id = idService.generateDonorId();
    d.setDonorId(id);
    int status = donorRepository.save(id, parentId, d.getDonorSubmitterId(), d.getDonorGender().toString());
    if (status != 1) {
      return "error: Can't create" + d.toString();
    }
    d.getSpecimens().forEach(s -> specimenService.create(id, s));

    return "ok:" + id;
  }

  public String update(String studyId, Donor d) {
    if (donorRepository.set(d.getDonorId(), studyId, d.getDonorSubmitterId(), d.getDonorGender().toString()) == 1) {
      return "Updated";
    }
    return "Failed";
  }

  public String delete(String studyId, String id) {
    specimenService.deleteByParentId(id);
    donorRepository.delete(studyId, id);
    return "OK";
  }

  public String deleteByParentId(String studyId) {
    donorRepository.getIds(studyId).forEach(id -> delete(studyId, id));

    return "OK";
  }

  public Donor getById(String studyId, String id) {
    val donor = donorRepository.getById(studyId, id);
    if (donor == null) {
      return null;
    }
    donor.setSpecimens(specimenService.findByParentId(id));
    return donor;
  }

  public List<Donor> findByParentId(String parentId) {
    val donors = donorRepository.findByParentId(parentId);
    donors.forEach(d -> d.setSpecimens(specimenService.findByParentId(d.getDonorId())));
    return donors;
  }

}