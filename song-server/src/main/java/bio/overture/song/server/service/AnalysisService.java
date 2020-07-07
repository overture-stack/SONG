package bio.overture.song.server.service;

import bio.overture.song.core.model.enums.AnalysisStates;
import bio.overture.song.server.model.analysis.Analysis;
import bio.overture.song.server.model.dto.Payload;
import bio.overture.song.server.model.entity.FileEntity;
import bio.overture.song.server.model.entity.composites.CompositeEntity;
import bio.overture.song.server.repository.search.IdSearchRequest;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import org.springframework.http.ResponseEntity;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface AnalysisService {

  String create(String studyId, Payload payload);

  void updateAnalysis( String studyId, String analysisId, JsonNode updateAnalysisRequest);

  List<Analysis> getAnalysis(String studyId, Set<String> analysisStates);

  List<Analysis> idSearch(String studyId, IdSearchRequest request);

  boolean isAnalysisExist(String id);

  void checkAnalysisExists(String id);

  void checkAnalysisAndStudyRelated(String studyId, String id);

  List<Analysis> unsecuredDeepReads(Collection<String> ids);

  Analysis unsecuredDeepRead(String id);

  List<FileEntity> unsecuredReadFiles(String id);

  ResponseEntity<String> publish( String studyId, String id, boolean ignoreUndefinedMd5);

  ResponseEntity<String> unpublish(String studyId, String id);

  ResponseEntity<String> suppress(String studyId, String id);

  List<CompositeEntity> readSamples(String id);

  AnalysisStates readState(String id);

  default List<FileEntity> securedReadFiles(@NonNull String studyId, String id) {
    checkAnalysisAndStudyRelated(studyId, id);
    return unsecuredReadFiles(id);
  }

  /**
   * Securely reads an analysis WITH all of its files, samples and info, and verifies the input
   * studyId is related to the requested analysisId
   */
  default Analysis securedDeepRead(@NonNull String studyId, String id) {
    checkAnalysisAndStudyRelated(studyId, id);
    return unsecuredDeepRead(id);
  }

}
