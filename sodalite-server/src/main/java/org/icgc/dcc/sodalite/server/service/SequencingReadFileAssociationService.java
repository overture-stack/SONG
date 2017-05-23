package org.icgc.dcc.sodalite.server.service;

import java.util.List;

import org.icgc.dcc.sodalite.server.model.File;
import org.icgc.dcc.sodalite.server.model.SequencingRead;
import org.icgc.dcc.sodalite.server.repository.SequencingReadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class SequencingReadFileAssociationService {

  @Autowired
  IdService idService;
  @Autowired
  SequencingReadRepository repository;

  public String associate(SequencingRead read, File file) {
    val associationId = idService.generateSequenceReadFileAssociationId();
    // TODO: verify study id is the same in both objects
    val status = repository.saveAssociation(associationId, read.getStudyId(), read.getAnalysisId(), file.getObjectId());
    if (status != 1) {
      throw new DatabaseRepositoryException(String.format("Could not associate Sequencing Read id %s with File id %s",
          read.getAnalysisId(), file.getObjectId()));
    }

    return associationId;
  }

  public void update(String associationId, SequencingRead read, File file) {
    if (repository.updateAssociation(associationId, read.getStudyId(), read.getAnalysisId(), file.getObjectId()) != 1) {
      throw new DatabaseRepositoryException(String.format("Could not associate Sequencing Read id %s with File id %s",
          read.getAnalysisId(), file.getObjectId()));
    }
  }

  public void delete(String id) {
    if (repository.delete(id) < 1) {
      throw new DatabaseRepositoryException(
          String.format("Could not delete SequencingRead/File Association id %s", id));
    }
  }

  public SequencingRead getById(String id) {
    val read = repository.getById(id);
    if (read == null) {
      log.info("Sequencing Read/File Association with id {} not found", id);
    }
    return read;
  }

  public List<String> getFileIds(String analysisId) {
    return repository.getFileIds(analysisId);
  }
}