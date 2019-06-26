package bio.overture.song.server.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableSet;
import lombok.val;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaClient;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
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
import static bio.overture.song.core.utils.JsonUtils.readTree;

@Configuration
public class SchemaConfig {

  public static final Set<String> SCHEMA_IDS = ImmutableSet.of("analysisRegistration", "analysisPayload");

  // schema dir
//  private static final Path SCHEMA_PATH = Paths.get("src/main/resources/schemas/analysis");
  private static final Path SCHEMA_PATH = Paths.get("schemas/analysis");

  private static Schema getSchema(String jsonSchemaFilename) throws IOException, JSONException {
    return SchemaLoader.builder()
        .schemaClient(SchemaClient.classPathAwareClient())
        .resolutionScope("classpath://"+SCHEMA_PATH.toString()+"/")
        .schemaJson(getSchemaJson(jsonSchemaFilename))
        .draftV7Support()
        .build()
        .load()
        .build();
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

  @Bean
  public JsonNode definitionsSchema() throws IOException {
    val schemaRelativePath = SCHEMA_PATH.resolve("definitions.json").toString();
    return readTree(getResourceContent(schemaRelativePath));
  }

  @Bean
  public String analysisBasePayloadSchemaContent() throws IOException, JSONException {
    val schemaRelativePath = SCHEMA_PATH.resolve("analysisPayload.json").toString();
    return getResourceContent(schemaRelativePath);
  }

}
