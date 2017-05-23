package org.icgc.dcc.sodalite.server.service;

import org.icgc.dcc.sodalite.server.model.SequencingRead;
import org.icgc.dcc.sodalite.server.repository.SequencingReadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.NoArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@NoArgsConstructor
public class SequencingReadService {

  @Autowired
  IdService idService;
  @Autowired
  SequencingReadRepository repository;

  public String create(SequencingRead read) {
    val analysisId = idService.generateSequenceReadId();
    // TODO: modifying input parameter.....
    read.setAnalysisId(analysisId);
    val status = repository.save(read.getAnalysisId(), read.getStudyId(), read.getAnalysisSubmitterId(),
        read.getState().toString(), read.getLibraryStrategy().toString(),
        read.isPairedEnd(), read.getInsertSize(), read.isAligned(), read.getAlignmentTool(), read.getReferenceGenome());
    if (status != 1) {
      return "error: Can't create" + read.toString();
    }
    return analysisId;
  }

  public void update(SequencingRead read) {
    repository.update(read.getAnalysisId(), read.getStudyId(), read.getAnalysisSubmitterId(),
        read.getState().toString(), read.getLibraryStrategy().toString(),
        read.isPairedEnd(), read.getInsertSize(), read.isAligned(), read.getAlignmentTool(), read.getReferenceGenome());
  }

  public void delete(String id) {
    repository.deleteAllAssociations(id);
    log.info(String.format("About to delete Sequencing Read with id %s", id));
    repository.delete(id);
  }

  public SequencingRead getById(String id) {
    val read = repository.getById(id);
    if (read == null) {
      return null;
    }
    return read;
  }

  public SequencingRead findByBusinessKey(String studyId, String analysisSubmitterId) {
    return repository.getByBusinessKey(studyId, analysisSubmitterId);
  }

}
