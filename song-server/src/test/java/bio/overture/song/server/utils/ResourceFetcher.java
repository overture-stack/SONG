package bio.overture.song.server.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Thread.currentThread;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;

@Value
@Builder
public class ResourceFetcher {

  @NonNull private final ResourceType resourceType;
  @NonNull private final Path dataDir;

  private Path getResourceDir(){
    return Paths.get("src/"+resourceType.toString()+"/resources");
  }

  public void check(){
    val path = getResourceDir().resolve(dataDir);
    checkArgument(exists(path) && isDirectory(path),
        "The test directory '%s' does not exist",
        path.toString());
  }

  public JsonNode readJsonNode(@NonNull String filename) throws IOException {
    return getJsonNodeFromClasspath(dataDir.resolve(filename).toString());
  }

  private static JsonNode getJsonNodeFromClasspath(String name) throws IOException {
    val is1 = currentThread().getContextClassLoader().getResourceAsStream(name);
    return new ObjectMapper().readTree(is1);
  }

  @RequiredArgsConstructor
  public enum ResourceType{
    TEST("test"),
    MAIN("main");

    private final String label;

    public String toString(){
      return label;
    }
  }

}
