package bio.overture.song.server.config;

import static bio.overture.song.core.utils.JsonUtils.readTree;
import static bio.overture.song.server.utils.JsonObjects.convertToJSONObject;
import static bio.overture.song.server.utils.JsonSchemas.buildSchema;
import static bio.overture.song.server.utils.Resources.getResourceContent;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import lombok.val;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaClient;
import org.everit.json.schema.loader.SchemaLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SchemaConfig {

  public static final Path SCHEMA_PATH = Paths.get("schemas/analysis");
  private static final Schema ANALYSIS_TYPE_META_SCHEMA = buildAnalysisTypeMetaSchema();

  @Bean
  public Schema analysisRegistrationSchema() {
    return buildSchema(SCHEMA_PATH, "analysisRegistration.json");
  }

  @Bean
  public JsonNode definitionsSchema() throws IOException {
    return getSchemaAsJson("definitions.json");
  }

  @Bean
  public String analysisPayloadBaseJson() throws IOException {
    val schemaRelativePath = SCHEMA_PATH.resolve("analysisPayload.json").toString();
    return getResourceContent(schemaRelativePath);
  }

  // [rtisma] NOTE: When this is transformed into a bean, a stackoverflow occurs, probably due to
  // some spring integration issue https://github.com/everit-org/json-schema/issues/191. The
  // workaround is to create a bean that is a callback to the schema
  @Bean
  @SneakyThrows
  public Supplier<Schema> analysisTypeMetaSchemaSupplier() {
    return () -> ANALYSIS_TYPE_META_SCHEMA;
  }

  private static JsonNode getSchemaAsJson(String schemaFilename) throws IOException {
    val schemaRelativePath = SCHEMA_PATH.resolve(schemaFilename);
    return readTree(getResourceContent(schemaRelativePath.toString()));
  }

  @SneakyThrows
  private static Schema buildAnalysisTypeMetaSchema() {
    val filename = "analysisType.metaschema.json";
    val jsonSchema = convertToJSONObject(getSchemaAsJson(filename));
    return SchemaLoader.builder()
        .schemaClient(SchemaClient.classPathAwareClient())
        .draftV7Support()
        .schemaJson(jsonSchema)
        .build()
        .load()
        .build();
  }
}
