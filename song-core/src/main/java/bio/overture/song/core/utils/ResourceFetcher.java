package bio.overture.song.core.utils;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.readString;
import static bio.overture.song.core.utils.JsonDocUtils.getJsonNodeFromClasspath;

@Value
@Builder
public class ResourceFetcher {

  @NonNull private final ResourceType resourceType;
  @NonNull private final Path dataDir;

  private Path getResourceDir() {
    return Paths.get("src/" + resourceType.toString() + "/resources");
  }

  public void check() {
    val path = getResourceDir().resolve(dataDir);
    checkArgument(
        exists(path) && isDirectory(path),
        "The test directory '%s' does not exist",
        path.toString());
  }

  public Path getPath(@NonNull String filename){
    return dataDir.resolve(filename);
  }

  public InputStream inputStream(@NonNull String filename) throws IOException {
    return newInputStream(getPath(filename));
  }

  public String content(@NonNull String filename) throws IOException {
    return readString(getPath(filename));
  }

  public JsonNode readJsonNode(@NonNull String filename) {
    return getJsonNodeFromClasspath(getPath(filename).toString());
  }

  @RequiredArgsConstructor
  public enum ResourceType {
    TEST("test"),
    MAIN("main");

    private final String label;

    public String toString() {
      return label;
    }
  }
}
