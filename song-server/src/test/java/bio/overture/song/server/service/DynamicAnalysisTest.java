package bio.overture.song.server.service;

import bio.overture.song.server.config.SchemaConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Thread.currentThread;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class DynamicAnalysisTest {

//  private static final Path TEST_DIR = Paths.get("src/test/resources/documents/validation/dynamic-analysis-type/");
  private static final Path TEST_DIR = Paths.get("documents/validation/dynamic-analysis-type/");
  private static final Path RESOURCE_DIR = Paths.get("src/test/resources");

  @Autowired
  private UploadService uploadService;
  @Autowired
  private SchemaConfig schemaConfig;

  @BeforeClass
  public static void beforeClass(){
    val path = RESOURCE_DIR.resolve(TEST_DIR);
    checkArgument(exists(path) && isDirectory(path),
        "The test directory '%s' does not exist",
        path.toString());
  }

  @Test
  @SneakyThrows
  public void test1(){
    val goodSchema = getJsonNodeFromClasspath(TEST_DIR.resolve("good.schema.json").toString());
    val id = goodSchema.get("id").asText();
    val response = uploadService.register(id, goodSchema);
    val response2 = uploadService.register(id, goodSchema);
    log.info("sdfsf");
  }

  private static JsonNode getJsonNodeFromClasspath(String name) throws Exception {
    val is1 = currentThread().getContextClassLoader().getResourceAsStream(name);
    return new ObjectMapper().readTree(is1);
  }


}
