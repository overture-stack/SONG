package bio.overture.song.server.utils.contract;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

/**
 * Compares JSON documents with golden files stored under {@code src/test/resources/golden}.
 *
 * <p>Golden files record the exact JSON contract of the server (field names, types, null handling,
 * array order and values) and are reviewed like source code: a changed golden file needs a written
 * justification. Object key order is not compared.
 *
 * <p>Running the tests with {@code -Dgolden.update=true} writes the actual documents to the golden
 * files instead of comparing them.
 */
public final class GoldenFiles {

  // Relative to the module directory, which is the working directory surefire runs tests in, so
  // updates land in the source tree rather than in target/.
  private static final Path GOLDEN_ROOT = Paths.get("src", "test", "resources", "golden");

  private static final boolean UPDATE_GOLDEN_FILES = Boolean.getBoolean("golden.update");

  private static final ObjectMapper PRETTY_MAPPER =
      new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

  private GoldenFiles() {}

  /**
   * Asserts that {@code actual} equals the golden file {@code <goldenName>.json}.
   *
   * <p>Fails when the documents differ, and when the golden file does not exist and updating is not
   * enabled. With updating enabled, writes {@code actual} to the golden file and passes.
   *
   * @param goldenName path below the golden root, without the {@code .json} extension, such as
   *     {@code http/get-study}
   */
  @SneakyThrows
  public static void assertMatchesGolden(@NonNull String goldenName, @NonNull JsonNode actual) {
    val goldenPath = GOLDEN_ROOT.resolve(goldenName + ".json");
    val actualText = PRETTY_MAPPER.writeValueAsString(actual) + "\n";
    if (UPDATE_GOLDEN_FILES) {
      Files.createDirectories(goldenPath.getParent());
      Files.writeString(goldenPath, actualText, StandardCharsets.UTF_8);
      return;
    }
    if (!Files.exists(goldenPath)) {
      fail(
          String.format(
              "Golden file '%s' does not exist. Run with -Dgolden.update=true to create it. "
                  + "Actual document:%n%s",
              goldenPath, actualText));
    }
    val expectedText = Files.readString(goldenPath, StandardCharsets.UTF_8);
    try {
      assertJsonEquals(expectedText, actualText);
    } catch (AssertionError error) {
      throw new AssertionError(
          String.format(
              "Response does not match golden file '%s'.%n%s%nActual document:%n%s",
              goldenPath, error.getMessage(), actualText),
          error);
    }
  }
}
