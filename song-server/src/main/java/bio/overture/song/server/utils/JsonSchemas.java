package bio.overture.song.server.utils;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaClient;
import org.everit.json.schema.loader.SchemaLoader;

import java.nio.file.Path;

import static lombok.AccessLevel.PRIVATE;
import static bio.overture.song.server.utils.JsonObjects.convertToJSONObject;

@NoArgsConstructor(access = PRIVATE)
public class JsonSchemas {

  @SneakyThrows
  public static Schema buildSchema(@NonNull Path schemaDir, @NonNull String filePathname) {
    val jsonObject = convertToJSONObject(schemaDir.resolve(filePathname));
    return SchemaLoader.builder()
        .schemaClient(SchemaClient.classPathAwareClient())
        .resolutionScope("classpath://" + schemaDir.toString() + "/")
        .schemaJson(jsonObject)
        .draftV7Support()
        .build()
        .load()
        .build();
  }

  @SneakyThrows
  public static Schema buildSchema(JsonNode json) {
    val jsonObject = convertToJSONObject(json);
    return SchemaLoader.builder()
        .schemaClient(SchemaClient.classPathAwareClient())
        .schemaJson(jsonObject)
        .draftV7Support()
        .build()
        .load()
        .build();
  }

  @SneakyThrows
  public static void validateWithSchema(@NonNull Schema schema, @NonNull JsonNode j) {
    val jsonObject = convertToJSONObject(j);
    schema.validate(jsonObject);
  }
}
