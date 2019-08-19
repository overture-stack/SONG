package bio.overture.song.core.utils;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.nio.file.FileVisitOption.FOLLOW_LINKS;
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

  @SneakyThrows
  public Stream<String> streamFilePaths(@NonNull String relativeDirpath){
    val relativeDir = Paths.get(relativeDirpath);
    val dir = dataDir.equals(relativeDir)? dataDir : dataDir.resolve(relativeDir);
    checkState(exists(getResourceDir().resolve(dir)), "The path '%s' does not exist", dir );
    checkState(isDirectory(getResourceDir().resolve(dir)), "The path '%s' is not a directory", dir );
    return Files.walk(getResourceDir().resolve(dir),1, FOLLOW_LINKS)
        .filter(x -> !isDirectory(x))
        .map(x -> getResourceDir().resolve(dataDir).relativize(x))
        .map(Path::toString);
  }

  @SneakyThrows
  public Stream<String> streamFileContents(@NonNull String relativeDirpath){
    return streamFilePaths(relativeDirpath)
        .map(this::content);
  }

  @SneakyThrows
  public Stream<JsonNode> streamJsonFiles(@NonNull String relativeDirpath){
    return streamFilePaths(relativeDirpath)
        .filter(x -> x.endsWith(".json"))
        .map(this::readJsonNode);
  }

  @SneakyThrows
  public Stream<String> streamRootFilePaths(){
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
    return newInputStream(getPath(filename));
  }

  @SneakyThrows
  public String content(@NonNull String filename) {
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
