package org.icgc.dcc.song.importer.convert;

import lombok.val;
import org.bson.Document;
import org.icgc.dcc.song.importer.model.DccMetadata;
import org.icgc.dcc.song.importer.resolvers.AccessTypes;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.icgc.dcc.song.importer.model.DccMetadata.createDccMetadata;

public class DccMetadataConverter {

  private static final String _CLASS = "_class";
  private static final String _ID = "_id";
  private static final String ACCESS = "access";
  private static final String CREATED_TIME = "createdTime";
  private static final String FILENAME = "fileName";
  private static final String GNOS_ID = "gnosId";
  private static final String PROJECT_CODE = "projectCode";

  public static DccMetadata convert(Document document){
    return createDccMetadata(
        getCls(document),
        getId(document),
        getAccess(document),
        getCreatedTime(document),
        getFilename(document),
        getGnosId(document),
        getProjectCode(document)
    );
  }

  public static String getCls(Document document){
    return getRequiredAsString(document, _CLASS);
  }

  public static String getId(Document document){
    return getRequiredAsString(document, _ID);
  }

  public static AccessTypes getAccess(Document document){
    val accessType = getRequiredAsString(document, ACCESS);
    return AccessTypes.resolve(accessType);
  }

  public static long getCreatedTime(Document document){
    return getRequiredAsLong(document, CREATED_TIME);
  }

  public static String getFilename(Document document){
    return getRequiredAsString(document, FILENAME);
  }

  public static String getGnosId(Document document){
    return getRequiredAsString(document, GNOS_ID);
  }

  public static String getProjectCode(Document document){
    return getRequiredAsString(document, PROJECT_CODE);
  }

  private static Object getRequiredObject(Document document, String key){
    checkArgument(document.containsKey(key),
        "The required key '%s' does not exist in the document",
        key);
    val value = document.get(key);
    checkNotNull(value,
        "The value for the required key '%s' cannot be null", key);
    return value;
  }

  private static String getRequiredAsString(Document document, String key){
    return getRequiredObject(document, key).toString();
  }

  private static long getRequiredAsLong(Document document, String key){
    return Long.parseLong(getRequiredAsString(document, key));
  }

}
