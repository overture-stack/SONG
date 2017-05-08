package org.icgc.dcc.sodalite.server.service;

import java.util.List;

import org.icgc.dcc.sodalite.server.model.Donor;
import org.icgc.dcc.sodalite.server.model.Specimen;
import org.icgc.dcc.sodalite.server.repository.DonorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
@Service
public class DonorService extends AbstractEntityService<Donor> {

  @Autowired
  DonorRepository donorRepository;
  @Autowired
  IdService idService;
  @Autowired
  SpecimenService specimenService;

  @Override
  public String create(String parentId, Donor d) {
    String id = idService.generateDonorId();
    d.setDonorId(id);
    int status = donorRepository.save(id, parentId, d.getDonorSubmitterId(), d.getDonorGender().toString());
    if (status != 1) {
      return "error: Can't create" + d.toString();

    }
    for (Specimen s : d.getSpecimens()) {
      specimenService.create(id, s);
    }

    return "ok:" + id;
  }

  @Override
  public String update(Donor d) {
    if (donorRepository.set(d.getDonorId(), d.getDonorSubmitterId(), d.getDonorGender().toString()) == 1) {
      return "Updated";
    }
    return "Failed";
  }

  @Override
  public String delete(String id) {
    specimenService.deleteByParentId(id);
    donorRepository.delete(id);
    return "OK";
  }

  @Override
  public String deleteByParentId(String parentId) {
    List<String> ids = donorRepository.getIds(parentId);
    for (val id : ids) {
      delete(id);
    }
    return "OK";
  }

  @Override
  public Donor getById(String id) {
    Donor donor = donorRepository.getById(id);
    if (donor == null) {
      return null;
    }
    donor.setSpecimens(specimenService.findByParentId(id));
    return donor;
  }

  @Override
  public List<Donor> findByParentId(String parentId) {
    List<Donor> donors = donorRepository.findByParentId(parentId);
    for (Donor d : donors) {
      d.setSpecimens(specimenService.findByParentId(d.getDonorId()));
    }
    return donors;
  }

}