package bio.overture.song.server.service;

import bio.overture.song.server.model.entity.AnalysisType;
import bio.overture.song.server.repository.AnalysisTypeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.everit.json.schema.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_TYPE_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerException.checkServer;
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

  public JsonNode getSchema(@NonNull String name, @NonNull Integer version){

    checkServer(version > 0,getClass(), ANALYSIS_TYPE_NOT_FOUND,
        "The version '%s' must be greater than 0", version);

    // TODO: [rtisma] query executes correct select with offset and limit, but followed by second count query.
    //  Try to remove the count query
    val page = analysisTypeRepository.findAllByName(name, PageRequest.of(version-1, 1,Sort.by(Sort.Direction.DESC, "id")));
    val latestVersion = page.getTotalElements();
    val analysisTypeNameExists = page.getTotalElements() > 0;
    val analysisTypeVersionExists = version <= latestVersion;
    val analysisTypes = page.getContent();

    checkServer(analysisTypeNameExists, getClass(), ANALYSIS_TYPE_NOT_FOUND,
        "The analysisType with name '%s' does not exist",
        name);
    checkServer(analysisTypeVersionExists, getClass(), ANALYSIS_TYPE_NOT_FOUND,
        "Version '%s' of analysisType with name '%s' does not exist however exists for the latest version '%s'",
        name, version, latestVersion);
    checkState(analysisTypes.size() == 1, "Should not be here. Only 1 analysisType should be returned");
    return analysisTypes.get(0).getSchema();
  }

  public JsonNode getSchemaOLD(@NonNull String name, @NonNull Integer version){

    checkServer(version > 0,getClass(), ANALYSIS_TYPE_NOT_FOUND,
        "The version '%s' must be greater than 0", version);

    val analysisTypes = analysisTypeRepository.findAllByNameOrderByIdDesc(name);
    val latestVersion = analysisTypes.size();
    val analysisTypeNameExists = !analysisTypes.isEmpty();
    val analysisTypeVersionExists = version <= latestVersion;

    checkServer(analysisTypeNameExists, getClass(), ANALYSIS_TYPE_NOT_FOUND,
        "The analysisType with name '%s' does not exist",
        name);
    checkServer(analysisTypeVersionExists, getClass(), ANALYSIS_TYPE_NOT_FOUND,
        "The analysisType with name '%s' and version '%s' does not exist, however exists for the latest version '%s'",
        name, version, latestVersion);
    return analysisTypes.get(version-1).getSchema();
  }

  public Optional<JsonNode> findSchema(@NonNull String name, @NonNull Integer version){
    checkArgument(version > 0,
        "The version '%s' is not greater than 0", version);
    val analysisTypes = analysisTypeRepository.findAllByName(name, PageRequest.of(version, 1,Sort.by(Sort.Direction.DESC, "id")))
        .getContent();
    if (analysisTypes.isEmpty()){
      return Optional.empty();
    }
    return Optional.of(analysisTypes.get(0).getSchema());
  }

  public Optional<JsonNode> findSchemaOLD(@NonNull String name, @NonNull Integer version){
    val analysisTypes = analysisTypeRepository.findAllByNameOrderByIdDesc(name);
    checkArgument(version > 0,
        "The version '%s' is not greater than 0", version);
    if (analysisTypes.size() < version){
      return Optional.empty();
    }
    return Optional.of(analysisTypes.get(version-1).getSchema());
  }

  @SneakyThrows
  public static void validateWithSchema(@NonNull Schema schema, @NonNull JsonNode j){
    val jsonObject = toJsonObject(j);
    schema.validate(jsonObject);
  }

  public Optional<AnalysisType> findLatestAnalysisType(@NonNull String analysisTypeName){
    return analysisTypeRepository.findFirstByNameOrderByIdDesc(analysisTypeName);
  }

  public AnalysisType getLatestAnalysisType(@NonNull String analysisTypeName){
    return checkServerOptional(findLatestAnalysisType(analysisTypeName)
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

  public Set<String> listAnalysisTypeNames() {
    return analysisTypeRepository.findDistinctBy(AnalysisTypeNameView.class)
        .stream()
        .map(AnalysisTypeNameView::getName)
        .collect(toImmutableSet());
  }

  private Integer getLatestVersionNumber(String analysisTypeName){
    return analysisTypeRepository.countAllByName(analysisTypeName);
  }

  public interface  AnalysisTypeNameView{
    String getName();
  }

}
