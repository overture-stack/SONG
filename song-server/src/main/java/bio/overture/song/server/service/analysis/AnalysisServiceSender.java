package bio.overture.song.server.service.analysis;

import static bio.overture.song.core.model.enums.AnalysisActions.CREATE;
import static bio.overture.song.core.model.enums.AnalysisActions.PUBLISH;
import static bio.overture.song.core.model.enums.AnalysisActions.SUPPRESS;
import static bio.overture.song.core.model.enums.AnalysisActions.UNPUBLISH;
import static bio.overture.song.core.model.enums.AnalysisStates.PUBLISHED;
import static bio.overture.song.core.model.enums.AnalysisStates.SUPPRESSED;
import static bio.overture.song.core.model.enums.AnalysisStates.UNPUBLISHED;
import static bio.overture.song.core.utils.JsonUtils.toJson;
import static bio.overture.song.server.kafka.AnalysisMessage.createAnalysisMessage;

import bio.overture.song.core.model.enums.AnalysisActions;
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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
  @Transactional
  public Analysis create(String studyId, Payload payload) {
    val analysis = internalAnalysisService.create(studyId, payload);

    // Current internalAnalysisService is instance of AnalysisServiceImpl which has
    // `create` as transactional. This means the analysis returned is from memory not
    // the one committed to db which can be different (for example, model.entity.Info
    // has CustomJsonType on its `info` field which removes keys with null values
    // when writing to db). So if we are transactional, fetch the analysis after it is
    // committed to make sure we have the right one before sending a message with it.
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(
          new TransactionSynchronization() {
            public void afterCommit() {
              val committedAnalysis =
                  internalAnalysisService.securedDeepRead(
                      analysis.getStudyId(), analysis.getAnalysisId());
              sendAnalysisMessage(committedAnalysis, UNPUBLISHED, CREATE);
            }
          });
    } else {
      sendAnalysisMessage(analysis, UNPUBLISHED, CREATE);
    }
    return analysis;
  }

  @Override
  public Analysis publish(String studyId, String id, boolean ignoreUndefinedMd5) {
    val analysis = internalAnalysisService.publish(studyId, id, ignoreUndefinedMd5);
    sendAnalysisMessage(analysis, PUBLISHED, PUBLISH);
    return analysis;
  }

  @Override
  public Analysis unpublish(String studyId, String id) {
    val analysis = internalAnalysisService.unpublish(studyId, id);
    sendAnalysisMessage(analysis, UNPUBLISHED, UNPUBLISH);
    return analysis;
  }

  @Override
  public Analysis suppress(String studyId, String id) {
    val analysis = internalAnalysisService.suppress(studyId, id);
    sendAnalysisMessage(analysis, SUPPRESSED, SUPPRESS);
    return analysis;
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
      Analysis analysis, AnalysisStates analysisState, AnalysisActions action) {
    val message = createAnalysisMessage(action, analysis, songServerId);
    sender.send(toJson(message));
  }
}
