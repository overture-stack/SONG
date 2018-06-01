package org.icgc.dcc.song.server.repository;

import org.icgc.dcc.song.server.model.entity.BusinessKeyView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusinessKeyRepository extends JpaRepository<BusinessKeyView, String> {

  List<BusinessKeyView> findAllByStudyIdAndSpecimenSubmitterId(String studyId, String specimenSubmitterId);
  List<BusinessKeyView> findAllByStudyIdAndSampleSubmitterId(String studyId, String sampleSubmitterId);
  long countAllByStudyIdAndSpecimenId(String studyId, String specimenId);
  long countAllByStudyIdAndSampleId(String studyId, String sampleId);

}
