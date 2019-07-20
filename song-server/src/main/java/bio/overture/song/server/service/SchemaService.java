package bio.overture.song.server.service;

import bio.overture.song.server.model.entity.AnalysisType;
import bio.overture.song.server.repository.AnalysisTypeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.everit.json.schema.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_TYPE_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerException.checkServerOptional;
import static bio.overture.song.core.utils.JsonDocUtils.toJsonObject;

@Service
public class SchemaService {

  private final Schema payloadMetaSchema;
  private final AnalysisTypeRepository analysisTypeRepository;

  @Autowired
  public SchemaService(@NonNull Supplier<Schema> payloadMetaSchemaSupplier,
      @NonNull AnalysisTypeRepository analysisTypeRepository) {
    this.payloadMetaSchema = payloadMetaSchemaSupplier.get();
    this.analysisTypeRepository = analysisTypeRepository;
  }

  public Schema getPayloadMetaSchema(){
    return payloadMetaSchema;
  }

  public AnalysisType get(@NonNull String name, @NonNull Integer version){
    return checkServerOptional(find(name, version), getClass(),
        ANALYSIS_TYPE_NOT_FOUND,
        "The analysisType with name '%s' and version '%s' was not found",
        name, version);
  }

  public Optional<AnalysisType> find(@NonNull String name, @NonNull Integer version){
    val analysisTypes = analysisTypeRepository.findAllByNameOrderByIdDesc(name);
    checkArgument(version > 0,
        "The version '%s' is not greater than 0", version);
    if (analysisTypes.size() < version){
      return Optional.empty();
    }
    return Optional.of(analysisTypes.get(version-1));
  }

  @SneakyThrows
  public static void validateWithSchema(@NonNull Schema schema, @NonNull JsonNode j){
    val jsonObject = toJsonObject(j);
    schema.validate(jsonObject);
  }

  public AnalysisType getLatestAnalysisType(@NonNull String analysisTypeName){
    return checkServerOptional(analysisTypeRepository.findFirstByNameOrderByIdDesc(analysisTypeName)
        , getClass(), ANALYSIS_TYPE_NOT_FOUND,
        "The analysisType with name '%s' was not found", analysisTypeName);
  }

  // [TEST] - test calling this method will always create a new id
  public Integer commitAnalysisType(@NonNull String analysisTypeName, @NonNull JsonNode analysisTypeSchema) {
    val analysisType = AnalysisType.builder()
        .name(analysisTypeName)
        .schema(analysisTypeSchema)
        .build();
    analysisTypeRepository.save(analysisType);
    return getLatestVersionNumber(analysisTypeName);
  }

  private Integer getLatestVersionNumber(String analysisTypeName){
    return analysisTypeRepository.countAllByName(analysisTypeName);
  }

}
