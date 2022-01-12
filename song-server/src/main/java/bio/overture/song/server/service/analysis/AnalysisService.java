package bio.overture.song.server.service.analysis;

import bio.overture.song.core.model.enums.AnalysisStates;
import bio.overture.song.server.model.analysis.Analysis;
import bio.overture.song.server.model.dto.Payload;
import bio.overture.song.server.model.entity.FileEntity;
import bio.overture.song.server.model.entity.composites.CompositeEntity;
import bio.overture.song.server.repository.search.IdSearchRequest;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import lombok.NonNull;

public interface AnalysisService {

  Analysis create(String studyId, Payload payload);

  void updateAnalysis(String studyId, String analysisId, JsonNode updateAnalysisRequest);

  List<Analysis> getAnalysis(String studyId, Set<String> analysisStates);

  GetAnalysisResponse getAnalysis(
      String studyId, Set<String> analysisStates, int limit, int offset);

  List<Analysis> idSearch(String studyId, IdSearchRequest request);

  boolean isAnalysisExist(String id);

  void checkAnalysisExists(String id);

  void checkAnalysisAndStudyRelated(String studyId, String id);

  List<Analysis> unsecuredDeepReads(Collection<String> ids);

  Analysis unsecuredDeepRead(String id);

  List<FileEntity> unsecuredReadFiles(String id);

  Analysis publish(String studyId, String id, boolean ignoreUndefinedMd5);

  Analysis unpublish(String studyId, String id);

  Analysis suppress(String studyId, String id);

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
