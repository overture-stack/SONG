package bio.overture.song.server.service;

import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.repository.AnalysisSchemaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Thread.currentThread;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static org.assertj.core.api.Assertions.assertThat;
import static bio.overture.song.core.exceptions.ServerErrors.SCHEMA_VIOLATION;
import static bio.overture.song.core.testing.SongErrorAssertions.assertSongError;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class DynamicAnalysisTest {

  private static final Path TEST_DIR = Paths.get("documents/validation/dynamic-analysis-type/");
  private static final Path RESOURCE_DIR = Paths.get("src/test/resources");
  private static final String GOOD_SCHEMA_FILENAME = "good.schema.json";
  private static final String BAD_SCHEMA_FILENAME = "bad.schema.json";

  @Autowired private UploadService uploadService;
  @Autowired private AnalysisSchemaRepository analysisSchemaRepository;
  private RandomGenerator randomGenerator;

  @Before
  public void beforeTest(){
    this.randomGenerator = createRandomGenerator(getClass().getSimpleName());
  }

  @BeforeClass
  public static void beforeClass(){
    val path = RESOURCE_DIR.resolve(TEST_DIR);
    checkArgument(exists(path) && isDirectory(path),
        "The test directory '%s' does not exist",
        path.toString());
  }

  @Test
  public void incrementingRegistrationVersionNumber_MultipleGoodSchemas_Success() throws IOException {
    val goodSchema = readJsonNode(GOOD_SCHEMA_FILENAME);

    // Create analysisType1 and assert version 1
    val analysisTypeName1 = generateUniqueAnalysisSchemaName();
    val resp1 = uploadService.register(analysisTypeName1, goodSchema);
    assertThat(resp1.getName()).isEqualTo(analysisTypeName1);
    assertThat(resp1.getVersion()).isEqualTo(1);

    // Create analysisType2 and assert version 1
    val analysisTypeName2 = generateUniqueAnalysisSchemaName();
    val resp2 = uploadService.register(analysisTypeName2, goodSchema);
    assertThat(resp2.getName()).isEqualTo(analysisTypeName2);
    assertThat(resp2.getVersion()).isEqualTo(1);

    // Create analysisType1 and assert version 2
    val resp3 = uploadService.register(analysisTypeName1, goodSchema);
    assertThat(resp3.getName()).isEqualTo(analysisTypeName1);
    assertThat(resp3.getVersion()).isEqualTo(2);

    // Create analysisType1 and assert version 3
    val resp4 = uploadService.register(analysisTypeName1, goodSchema);
    assertThat(resp4.getName()).isEqualTo(analysisTypeName1);
    assertThat(resp4.getVersion()).isEqualTo(3);

    // Create analysisType2 and assert version 2
    val resp5 = uploadService.register(analysisTypeName2, goodSchema);
    assertThat(resp5.getName()).isEqualTo(analysisTypeName2);
    assertThat(resp5.getVersion()).isEqualTo(2);
  }

  @Test
  public void incrementingRegistrationVersionNumber_BadSchema_BadRequest() throws IOException {
    val badSchema = readJsonNode(BAD_SCHEMA_FILENAME);
    val analysisTypeName1 = generateUniqueAnalysisSchemaName();
    assertSongError(() ->
        uploadService.register(analysisTypeName1, badSchema), SCHEMA_VIOLATION);
  }

  private String generateUniqueAnalysisSchemaName(){
    String analysisType = null;
    do{
      analysisType = randomGenerator.generateRandomAsciiString(10);
    } while (analysisSchemaRepository.countAllByName(analysisType) > 0);
    return analysisType;
  }

  private static JsonNode readJsonNode(String filename) throws IOException {
    return getJsonNodeFromClasspath(TEST_DIR.resolve(filename).toString());
  }

  private static JsonNode getJsonNodeFromClasspath(String name) throws IOException {
    val is1 = currentThread().getContextClassLoader().getResourceAsStream(name);
    return new ObjectMapper().readTree(is1);
  }

}
