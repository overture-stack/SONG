package org.icgc.dcc.song.server.service;

import lombok.SneakyThrows;
import lombok.val;
import org.icgc.dcc.song.server.model.entity.composites.StudyWithDonors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudyWithDonorsService {

  @Autowired
  private StudyService studyService;

  @Autowired
  private DonorService donorService;

  @SneakyThrows
  public StudyWithDonors readWithChildren(String studyId) {
    val study = new StudyWithDonors();
    val s = studyService.read(studyId);

    study.setStudy(s);
    study.setDonors(donorService.readByParentId(studyId));
    return study;
  }

}
