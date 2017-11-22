package org.icgc.dcc.song.importer.convert;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import lombok.val;
import org.icgc.dcc.song.importer.model.DccMetadata;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.server.model.enums.AccessTypes;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.icgc.dcc.song.core.utils.JsonUtils.toPrettyJson;
import static org.icgc.dcc.song.importer.model.DccMetadata.createDccMetadata;
import static org.icgc.dcc.song.server.model.enums.AccessTypes.resolveAccessType;

public class DccMetadataConverter {

  private static final String ID = "id";
  private static final String ACCESS = "access";
  private static final String CREATED_TIME = "createdTime";
  private static final String FILENAME = "fileName";
  private static final String GNOS_ID = "gnosId";
  private static final String PROJECT_CODE = "projectCode";

  public static DccMetadata convertToDccMetadata(JsonNode json){
    return createDccMetadata(
        getId(json),
        getAccess(json),
        getCreatedTime(json),
        getFilename(json),
        getGnosId(json),
        getProjectCode(json).orElse(null)
    );
  }

  /**
   *  JsonNode conversion
   */
  public static String getId(@NonNull JsonNode json){
    return getRequiredAsString(json, ID);
  }

  public static AccessTypes getAccess(@NonNull JsonNode json){
    val accessType = getRequiredAsString(json, ACCESS);
    return resolveAccessType(accessType);
  }

  public static long getCreatedTime(JsonNode json){
    return getRequiredAsLong(json, CREATED_TIME);
  }

  public static String getFilename(JsonNode json){
    return getRequiredAsString(json, FILENAME);
  }

  public static String getGnosId(JsonNode json){
    return getRequiredAsString(json, GNOS_ID);
  }

  public static Optional<String> getProjectCode(JsonNode json){
    return getOptionalAsString(json, PROJECT_CODE);
  }


  private static JsonNode getRequiredObject(JsonNode json, String field){
    checkArgument(json.has(field),
        "The required field '%s' does not exist in the json: \n%s",
        field, toPrettyJson(json));
    val value = json.get(field);
    checkNotNull(value,
        "The value for the required key '%s' cannot be null", field);
    return value;
  }

  private static Optional<JsonNode> getOptionalObject(JsonNode json, String field){
    return Optional.ofNullable(json.get(field));
  }


  private static String getRequiredAsString(JsonNode json, String field){
    return getRequiredObject(json,field).textValue();
  }


  private static Optional<String> getOptionalAsString(JsonNode json, String field){
    val o = getOptionalObject(json,field);
    return o.map(JsonNode::textValue);
  }

  private static long getRequiredAsLong(JsonNode json, String field){
    val value = getRequiredObject(json, field);
    return value.longValue();
  }

  public static String getId(@NonNull PortalFileMetadata portalFileMetadata) {
    return portalFileMetadata.getRepoMetadataPath()
        .trim()
        .replaceAll(".*\\/", "");
  }

}
