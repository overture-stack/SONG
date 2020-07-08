package bio.overture.song.server.service.analysis;

import static bio.overture.song.core.model.enums.AnalysisStates.PUBLISHED;
import static bio.overture.song.core.model.enums.AnalysisStates.SUPPRESSED;
import static bio.overture.song.core.model.enums.AnalysisStates.UNPUBLISHED;
import static bio.overture.song.core.utils.JsonUtils.toJson;
import static bio.overture.song.server.kafka.AnalysisMessage.createAnalysisMessage;

import bio.overture.song.core.model.enums.AnalysisStates;
import bio.overture.song.server.kafka.Sender;
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
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Primary
public class AnalysisServiceSender implements AnalysisService {

  private final String songServerId;
  private final Sender sender;
  private final AnalysisService internalAnalysisService;

  @Autowired
  public AnalysisServiceSender(
      @Value("${song.id}") @NonNull String songServerId,
      @NonNull Sender sender,
      @NonNull AnalysisService analysisServiceImpl) {
    this.songServerId = songServerId;
    this.sender = sender;
    this.internalAnalysisService = analysisServiceImpl;
  }

  /** Decorated methods */
  @Override
  public String create(String studyId, Payload payload) {
    val id = internalAnalysisService.create(studyId, payload);
    sendAnalysisMessage(id, studyId, UNPUBLISHED);
    return id;
  }

  @Override
  public ResponseEntity<String> publish(String studyId, String id, boolean ignoreUndefinedMd5) {
    val resp = internalAnalysisService.publish(studyId, id, ignoreUndefinedMd5);
    sendAnalysisMessage(id, studyId, PUBLISHED);
    return resp;
  }

  @Override
  public ResponseEntity<String> unpublish(String studyId, String id) {
    val resp = internalAnalysisService.unpublish(studyId, id);
    sendAnalysisMessage(id, studyId, UNPUBLISHED);
    return resp;
  }

  @Override
  public ResponseEntity<String> suppress(String studyId, String id) {
    val resp = internalAnalysisService.suppress(studyId, id);
    sendAnalysisMessage(id, studyId, SUPPRESSED);
    return resp;
  }

  /** Delegated methods */
  @Override
  public void updateAnalysis(String studyId, String analysisId, JsonNode updateAnalysisRequest) {
    internalAnalysisService.updateAnalysis(studyId, analysisId, updateAnalysisRequest);
  }

  @Override
  public List<Analysis> getAnalysis(String studyId, Set<String> analysisStates) {
    return internalAnalysisService.getAnalysis(studyId, analysisStates);
  }

  @Override
  public List<Analysis> idSearch(String studyId, IdSearchRequest request) {
    return internalAnalysisService.idSearch(studyId, request);
  }

  @Override
  public boolean isAnalysisExist(String id) {
    return internalAnalysisService.isAnalysisExist(id);
  }

  @Override
  public void checkAnalysisExists(String id) {
    internalAnalysisService.checkAnalysisExists(id);
  }

  @Override
  public void checkAnalysisAndStudyRelated(String studyId, String id) {
    internalAnalysisService.checkAnalysisAndStudyRelated(studyId, id);
  }

  @Override
  public List<Analysis> unsecuredDeepReads(Collection<String> ids) {
    return internalAnalysisService.unsecuredDeepReads(ids);
  }

  @Override
  public Analysis unsecuredDeepRead(String id) {
    return internalAnalysisService.unsecuredDeepRead(id);
  }

  @Override
  public List<FileEntity> unsecuredReadFiles(String id) {
    return internalAnalysisService.unsecuredReadFiles(id);
  }

  @Override
  public List<CompositeEntity> readSamples(String id) {
    return internalAnalysisService.readSamples(id);
  }

  @Override
  public AnalysisStates readState(String id) {
    return internalAnalysisService.readState(id);
  }

  @Override
  public List<FileEntity> securedReadFiles(@NonNull String studyId, String id) {
    return internalAnalysisService.securedReadFiles(studyId, id);
  }

  @Override
  public Analysis securedDeepRead(@NonNull String studyId, String id) {
    return internalAnalysisService.securedDeepRead(studyId, id);
  }

  private void sendAnalysisMessage(
      String studyId, String analysisId, AnalysisStates analysisState) {
    val message = createAnalysisMessage(analysisId, studyId, analysisState, songServerId);
    sender.send(toJson(message));
  }
}
