package bio.overture.song.server.service;

import bio.overture.song.server.model.entity.AnalysisType;
import bio.overture.song.server.repository.AnalysisTypeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.everit.json.schema.Schema;

import java.util.Optional;
import java.util.UUID;

import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_TYPE_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerException.checkServerOptional;
import static bio.overture.song.core.utils.JsonDocUtils.toJsonObject;

public class SchemaService {

  private final Schema payloadMetaSchema;
  private final AnalysisTypeRepository analysisTypeRepository;

  public SchemaService(Schema payloadMetaSchema,
      AnalysisTypeRepository analysisTypeRepository) {
    this.payloadMetaSchema = payloadMetaSchema;
    this.analysisTypeRepository = analysisTypeRepository;
  }

  public Schema getPayloadMetaSchema(){
    return payloadMetaSchema;
  }

  public AnalysisType get(@NonNull UUID id){
    return checkServerOptional(find(id), getClass(),
        ANALYSIS_TYPE_NOT_FOUND,
        "The analysisType with id '%s' was not found", id);
  }

  public Optional<AnalysisType> find(@NonNull UUID id){
    return analysisTypeRepository.findById(id);
  }

  @SneakyThrows
  public static void validateWithSchema(@NonNull Schema schema, @NonNull JsonNode j){
    val jsonObject = toJsonObject(j);
    schema.validate(jsonObject);
  }

  public AnalysisType getLatestAnalysisType(@NonNull String analysisTypeName){
    return checkServerOptional(analysisTypeRepository.findByNameOrderByVersionDesc(analysisTypeName)
        , getClass(), ANALYSIS_TYPE_NOT_FOUND,
        "The analysisType with name '%s' was not found", analysisTypeName);
  }

  // [TEST] - test calling this method will always create a new id
  public AnalysisType commitAnalysisType(@NonNull String analysisTypeName, @NonNull JsonNode analysisTypeSchema) {
    val analysisType = AnalysisType.builder()
        .name(analysisTypeName)
        .schema(analysisTypeSchema)
        .build();
    return analysisTypeRepository.save(analysisType);
  }
}
