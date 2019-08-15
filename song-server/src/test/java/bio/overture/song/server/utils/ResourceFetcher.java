package bio.overture.song.server.utils;

import static bio.overture.song.core.utils.JsonDocUtils.getJsonNodeFromClasspath;
import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;

import com.fasterxml.jackson.databind.JsonNode;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;

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

  public JsonNode readJsonNode(@NonNull String filename) {
    return getJsonNodeFromClasspath(dataDir.resolve(filename).toString());
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
