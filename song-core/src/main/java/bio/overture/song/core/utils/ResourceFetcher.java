package bio.overture.song.core.utils;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.nio.file.FileVisitOption.FOLLOW_LINKS;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;

@Value
@Builder
public class ResourceFetcher {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @NonNull private final ResourceType resourceType;
  @NonNull private final Path dataDir;

  private Path getResourceDir() {
    return Paths.get("src/" + resourceType.toString() + "/resources");
  }

  @SneakyThrows
  public Stream<String> streamFilePaths(@NonNull String relativeDirpath) {
    val relativeDir = Paths.get(relativeDirpath);
    val dir = dataDir.equals(relativeDir) ? dataDir : dataDir.resolve(relativeDir);
    checkState(exists(getResourceDir().resolve(dir)), "The path '%s' does not exist", dir);
    checkState(isDirectory(getResourceDir().resolve(dir)), "The path '%s' is not a directory", dir);
    return Files.walk(getResourceDir().resolve(dir), 1, FOLLOW_LINKS)
        .filter(x -> !isDirectory(x))
        .map(x -> getResourceDir().resolve(dataDir).relativize(x))
        .map(Path::toString);
  }

  @SneakyThrows
  public Stream<String> streamFileContents(@NonNull String relativeDirpath) {
    return streamFilePaths(relativeDirpath).map(this::content);
  }

  @SneakyThrows
  public Stream<JsonNode> streamJsonFiles(@NonNull String relativeDirpath) {
    return streamFilePaths(relativeDirpath)
        .filter(x -> x.endsWith(".json"))
        .map(this::readJsonNode);
  }

  @SneakyThrows
  public Stream<String> streamRootFilePaths() {
    return streamFilePaths(dataDir.toString());
  }

  public void check() {
    val path = getResourceDir().resolve(dataDir);
    checkArgument(
        exists(path) && isDirectory(path),
        "The test directory '%s' does not exist",
        path.toString());
  }

  public Path getPath(@NonNull String filename) {
    return dataDir.resolve(filename);
  }

  public InputStream inputStream(@NonNull String filename) throws IOException {
    return Thread.currentThread()
        .getContextClassLoader()
        .getResourceAsStream(getPath(filename).toString());
  }

  @SneakyThrows
  public String content(@NonNull String filename) {
    return Strings.toString(inputStream(filename));
  }

  @SneakyThrows
  public JsonNode readJsonNode(@NonNull String filename) {
    return OBJECT_MAPPER.readTree(inputStream(filename));
  }

  @SneakyThrows
  public <T> T readObject(@NonNull String filename, @NonNull Class<T> type) {
    return OBJECT_MAPPER.readValue(inputStream(filename), type);
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
