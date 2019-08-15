package bio.overture.song.server.utils;

import static bio.overture.song.server.utils.Resources.getResourceContent;
import static lombok.AccessLevel.PRIVATE;

import java.io.IOException;
import java.nio.file.Path;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaClient;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

@NoArgsConstructor(access = PRIVATE)
public class JsonSchemas {

  public static JSONObject getJSONObject(@NonNull Path filePath) throws IOException, JSONException {
    val content = getResourceContent(filePath.toString());
    return new JSONObject(new JSONTokener(content));
  }

  @SneakyThrows
  public static Schema getSchema(@NonNull Path schemaDir, @NonNull String filePathname) {
    val jsonObject = getJSONObject(schemaDir.resolve(filePathname));
    return SchemaLoader.builder()
        .schemaClient(SchemaClient.classPathAwareClient())
        .resolutionScope("classpath://" + schemaDir.toString() + "/")
        .schemaJson(jsonObject)
        .draftV7Support()
        .build()
        .load()
        .build();
  }
}
