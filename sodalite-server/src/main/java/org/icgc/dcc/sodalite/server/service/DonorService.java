package org.icgc.dcc.sodalite.server.service;

import static org.icgc.dcc.sodalite.server.model.enums.IdPrefix.Donor;

import java.util.List;

import org.icgc.dcc.sodalite.server.model.entity.Donor;
import org.icgc.dcc.sodalite.server.model.enums.IdPrefix;
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

  public String create(Donor d) {
    val id = idService.generate(Donor);
    d.setDonorId(id);

    int status = donorRepository.create(d);
    if (status != 1) {
      return "error: Can't create" + d.toString();
    }
    d.getSpecimens().forEach(s -> specimenService.create(id, s));

    return "ok:" + id;
  }

  public Donor read(String id) {
    val donor = donorRepository.read(id);
    if (donor == null) {
      return null;
    }
    donor.setSpecimens(specimenService.readByParentId(id));
    return donor;
  }

  public List<Donor> readByParentId(String parentId) {
    val donors = donorRepository.readByParentId(parentId);
    donors.forEach(d -> d.setSpecimens(specimenService.readByParentId(d.getDonorId())));
    return donors;
  }

  public String update(Donor d) {
    if (donorRepository.update(d) == 1) {
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
    donorRepository.findByParentId(studyId).forEach(id -> delete(studyId, id));

    return "OK";
  }

  public String save(String studyId, Donor d) {
    d.setStudyId(studyId);

    String donorId = donorRepository.findByBusinessKey(studyId, d.getDonorSubmitterId());
    if (donorId == null) {
      donorId = idService.generate(IdPrefix.Donor);
      d.setDonorId(donorId);
      System.err.printf("Creating new donor with id=%s,gender='%s'\n", donorId, d.getDonorGender());
      donorRepository.create(d);
    } else {
      donorRepository.update(d);
    }
    return donorId;
  }

}