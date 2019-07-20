package bio.overture.song.server.service;

import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.server.model.ExperimentSchema;
import bio.overture.song.server.model.enums.ModelAttributeNames;
import bio.overture.song.server.repository.SchemaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.everit.json.schema.Schema;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_TYPE_ALREADY_EXISTS;
import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_TYPE_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerErrors.MALFORMED_PARAMETER;
import static bio.overture.song.core.exceptions.ServerException.buildServerException;
import static bio.overture.song.core.exceptions.ServerException.checkServer;
import static bio.overture.song.core.utils.JsonDocUtils.toJsonObject;
import static bio.overture.song.core.utils.JsonUtils.readTree;
import static bio.overture.song.core.utils.JsonUtils.toJson;
import static bio.overture.song.core.utils.JsonUtils.toJsonNode;
import static bio.overture.song.server.config.SchemaConfig.buildSchema;

@Service
public class SchemaServiceEXP {

  private final Schema analysisRegistrationSchema;
  private final String analysisBasePayloadSchemaContent;
  private final SchemaRepository schemaRepository;
  private final JsonNode definitionsSchema;

  @Autowired
  public SchemaServiceEXP(@NonNull Schema analysisRegistrationSchema,
      @NonNull String analysisBasePayloadSchemaContent,
      @NonNull JsonNode definitionsSchema,
      @NonNull SchemaRepository schemaRepository
      ) {
    this.analysisRegistrationSchema = analysisRegistrationSchema;
    this.schemaRepository = schemaRepository;
    this.definitionsSchema  = definitionsSchema;
    this.analysisBasePayloadSchemaContent = analysisBasePayloadSchemaContent;
  }

  public JsonNode getDefinitions(){
    return definitionsSchema;
  }

  public Set<String> listAnalysisTypes(){
    return schemaRepository.findAll().stream().map(ExperimentSchema::getAnalysisType).collect(toImmutableSet());
  }

  private void checkAnalysisTypeUnique(String analysisType ){
    checkServer(!schemaRepository.existsById(analysisType), getClass(),
        ANALYSIS_TYPE_ALREADY_EXISTS, "The analysisType '%s' already exists", analysisType);
  }

  @SneakyThrows
  public void registerAnalysis(@NonNull JsonNode j){
    validateAnalysisRegistration(j);
    val analysisType = j.get(ModelAttributeNames.ANALYSIS_TYPE).textValue();
    checkAnalysisTypeUnique(analysisType);
    val schema = j.get(ModelAttributeNames.EXPERIMENT_SCHEMA).toString();
    val experimentSchema = ExperimentSchema.builder()
        .analysisType(analysisType)
        .schema(JsonUtils.toMap(schema))
        .build();

    // Validate its ok
    combineExperimentAndBaseSchema(readTree(schema));
    schemaRepository.save(experimentSchema);
  }

  // TODO: add listener to describe errors
  public void validateAnalysisRegistration(@NonNull JsonNode j) {
    validateWithSchema(analysisRegistrationSchema, j);
  }

  private JsonNode resolveExperimentSchema(String analysisType){
    val experimentSchema = (ObjectNode)getSchema(analysisType);
    experimentSchema.put("additionalProperties", false);
    return experimentSchema;
  }

  @SneakyThrows
  private JsonNode combineExperimentAndBaseSchema(JsonNode experimentSchema){
    val outputSchema = readTree(analysisBasePayloadSchemaContent);
    val properties = (ObjectNode)outputSchema.path("properties");
    properties.put("experiment",experimentSchema);
    return outputSchema;

  }
  public JsonNode resolveAnalysisTypeJsonSchema(@NonNull String analysisType){
    val experimentSchema = resolveExperimentSchema(analysisType);
    return combineExperimentAndBaseSchema(experimentSchema);
  }

  // TODO: add listener to describe errors
  @SneakyThrows
  public void validatePayload(@NonNull JsonNode payload) {
    //extract analysisType
    checkServer(payload.has("analysisType"), getClass(),
        MALFORMED_PARAMETER, "The analysisType field is missing");
    val analysisType = payload.get("analysisType").asText();

    // get schema
    val resolvedSchema = buildSchema(convertToJsonObject(resolveAnalysisTypeJsonSchema(analysisType)));

    // validate
    resolvedSchema.validate(convertToJsonObject(payload));
  }


  public JsonNode getSchema(String analysisType) {
    val experimentSchema =  schemaRepository.findById(analysisType)
        .orElseThrow(() -> buildServerException(getClass(), ANALYSIS_TYPE_NOT_FOUND,
            "The schema for '%s' was not found", analysisType));
    return toJsonNode(experimentSchema.getSchema());
  }

  @SneakyThrows
  private static void validateWithSchema(Schema schema, JsonNode j){
    val jsonObject = toJsonObject(j);
    schema.validate(jsonObject);

  }

  public static JSONObject convertToJsonObject(@NonNull String s) throws JSONException {
    return new JSONObject(new JSONTokener(s));
  }

  public static JSONObject convertToJsonObject(@NonNull JsonNode j) throws JSONException {
    return convertToJsonObject(toJson(j));
  }


}
