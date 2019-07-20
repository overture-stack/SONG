package bio.overture.song.server.config;

import bio.overture.song.server.repository.AnalysisTypeRepository;
import bio.overture.song.server.service.SchemaService;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableSet;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaClient;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;
import static bio.overture.song.core.utils.JsonUtils.convertToJSONObject;
import static bio.overture.song.core.utils.JsonUtils.readTree;

@Configuration
public class SchemaConfig {

  private static final Path SCHEMA_PATH = Paths.get("schemas/analysis");
  public static final Schema PAYLOAD_META_SCHEMA = buildPayloadMetaSchema();
  public static final Set<String> SCHEMA_IDS = ImmutableSet.of("analysisRegistration", "analysisPayload");

  // schema dir
//  private static final Path SCHEMA_PATH = Paths.get("src/main/resources/schemas/analysis");

  public static Schema buildSchema(@NonNull JSONObject jsonSchema) throws IOException, JSONException {
    return SchemaLoader.builder()
        .schemaClient(SchemaClient.classPathAwareClient())
        .resolutionScope("classpath://"+SCHEMA_PATH.toString()+"/")
        .schemaJson(jsonSchema)
        .draftV7Support()
        .build()
        .load()
        .build();
  }

  public static Schema getSchema(String jsonSchemaFilename) throws IOException, JSONException {
    return buildSchema(getSchemaJson(jsonSchemaFilename));
  }

  public static String getResourceContent(String resourceFilename) throws IOException {
    try(val inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceFilename)) {
      if(isNull(inputStream)){
        throw new IOException(format("The classpath resource '%s' does not exist", resourceFilename));
      }
      return new BufferedReader(new InputStreamReader(inputStream))
          .lines()
          .collect(joining("\n"));
    }
  }

  private static JSONObject getSchemaJson(String jsonSchemaFilename) throws IOException, JSONException {
    val schemaRelativePath = SCHEMA_PATH.resolve(jsonSchemaFilename).toString();
    val content = getResourceContent(schemaRelativePath);
    return new JSONObject(new JSONTokener(content));
  }


  @Bean
  public Schema analysisRegistrationSchema() throws IOException, JSONException {
    return getSchema("analysisRegistration.json");
  }

  private static JsonNode getSchemaAsJson(String schemaFilename) throws IOException, JSONException {
    val schemaRelativePath = SCHEMA_PATH.resolve(schemaFilename);
//    checkArgument(exists(schemaRelativePath) && isRegularFile(schemaRelativePath),
//        "The file '%s' does not exist",
//        schemaRelativePath.toAbsolutePath().toString());
    return readTree(getResourceContent(schemaRelativePath.toString()));

  }



  @Bean
  public JsonNode definitionsSchema() throws IOException, JSONException {
    return getSchemaAsJson("definitions.json");
  }

  @Bean
  public String analysisBasePayloadSchemaContent() throws IOException, JSONException {
    val schemaRelativePath = SCHEMA_PATH.resolve("analysisPayload.json").toString();
    return getResourceContent(schemaRelativePath);
  }

  // [rtisma] NOTE: When this is transformed into a bean, a stackoverflow occurs, probably due to some spring integration issue https://github.com/everit-org/json-schema/issues/191. The workaround is to build the schema in a static context instead of an application context.
  @SneakyThrows
  private static Schema buildPayloadMetaSchema()  {
    val filename = "payload.metaschema.json";
    val jsonSchema = convertToJSONObject(getSchemaAsJson(filename));
    return SchemaLoader.builder()
        .schemaClient(SchemaClient.classPathAwareClient())
        .draftV7Support()
        .schemaJson(jsonSchema)
        .build()
        .load()
        .build();
  }

  @Bean
  public SchemaService schemaService(@Autowired AnalysisTypeRepository repository){
    return new SchemaService(PAYLOAD_META_SCHEMA, repository);
  }

}
