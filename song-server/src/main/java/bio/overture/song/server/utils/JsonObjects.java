package bio.overture.song.server.utils;

import static bio.overture.song.server.utils.Resources.getResourceContent;

import bio.overture.song.core.utils.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.nio.file.Path;
import lombok.NonNull;
import lombok.val;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JsonObjects {

  public static JSONObject convertToJSONObject(@NonNull String s) throws JSONException {
    return new JSONObject(new JSONTokener(s));
  }

  public static JSONObject convertToJSONObject(@NonNull JsonNode j) throws JSONException {
    return convertToJSONObject(JsonUtils.toJson(j));
  }

  public static JSONObject convertToJSONObject(@NonNull Path filePath)
      throws IOException, JSONException {
    val content = getResourceContent(filePath.toString());
    return convertToJSONObject(content);
  }
}
