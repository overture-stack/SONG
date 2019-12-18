/*
 * Copyright (c) 2019. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package bio.overture.song.server.validation;

import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.model.enums.ModelAttributeNames.FILES;
import static bio.overture.song.server.utils.TestFiles.getJsonNodeFromClasspath;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static org.icgc.dcc.common.core.util.Splitters.COMMA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.service.ValidationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;
import java.util.Map;
import lombok.val;
import org.icgc.dcc.common.core.util.Splitters;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class})
@ActiveProfiles("test")
public class ValidationServiceTest {

  private static final Map<String, String> DEFAULT_TEST_FILE_MAP = Maps.newHashMap();
  private static final String SEQ_READ = "SequencingRead";
  private static final String VAR_CALL = "VariantCall";

  static {
    DEFAULT_TEST_FILE_MAP.put(SEQ_READ, "sequencingRead.json");
    DEFAULT_TEST_FILE_MAP.put(VAR_CALL, "variantCall.json");
  }

  private final RandomGenerator randomGenerator =
      createRandomGenerator(ValidationServiceTest.class.getSimpleName());

  @Autowired private ValidationService service;

  @Test
  public void testValidateValidSequencingRead() {
    val payload = getJsonFile("sequencingRead.json");
    val results = service.validate(payload);
    assertFalse(results.isPresent());
  }

  @Test
  public void testValidateValidSequencingReadWithArchive() {
    val payload = getJsonFile("sequencingReadWithArchive.json");
    val results = service.validate(payload);
    assertFalse(results.isPresent());
  }

  @Test
  public void testValidateValidVariantCall() {
    val payload = getJsonFile("variantCall.json");
    val results = service.validate(payload);
    assertFalse(results.isPresent());
  }

  @Test
  public void testValidateVariantCallMissingAnalysisType() {
    val payload = getJsonFile("variantCall.json");
    ((ObjectNode) payload).put("analysisType", (String) null);
    val results = service.validate(payload);
    assertTrue(results.isPresent());
    assertTrue(results.get().contains("Missing the 'analysisType' field"));
  }

  @Test
  public void testValidateSequencingReadMissingAnalysisType() {
    val payload = getJsonFile("sequencingRead.json");
    ((ObjectNode) payload).put("analysisType", (String) null);
    val results = service.validate(payload);
    assertTrue(results.isPresent());
    assertTrue(results.get().contains("Missing the 'analysisType' field"));
  }

  @Test
  public void testFileMd5Validation() {
    val testMap = Maps.<String, String>newHashMap();
    testMap.put(SEQ_READ, "sequencingRead.json");
    testMap.put(VAR_CALL, "variantCall.json");

    for (val testDataEntry : testMap.entrySet()) {
      val testFileName = testDataEntry.getValue();

      val payload = getJsonFile(testFileName);
      val fileNodes = newArrayList(payload.path("files"));
      assertTrue(fileNodes.size() > 1);
      val fileNode0 =
          ((ObjectNode) fileNodes.get(0))
              .put("fileMd5sum", "q123"); // less than 32 and non-hex number
      val fileNode1 =
          ((ObjectNode) fileNodes.get(1))
              .put(
                  "fileMd5sum",
                  "q0123456789012345678901234567890123456789"); // more than 32 and non-hex number

      val results = service.validate(payload);

      assertTrue(results.isPresent());

      val errors = COMMA.splitToList(results.get());
      assertEquals(2, errors.size());
      for (val error : errors) {
        assertTrue(
            error.matches(
                "^#/files/[0|1]/fileMd5sum: string \\[[^\\]]+\\] does not match pattern.*"));
      }
    }
  }

  @Test
  public void testFileMd5sumValidation() {
    val md5 = randomGenerator.generateRandomMD5();
    assertEquals(md5.length(), 32);
    for (val schemaType : DEFAULT_TEST_FILE_MAP.keySet()) {
      runFileMd5sumValidationTest(md5 + "1", schemaType, true); // invalidate >32 chars
      runFileMd5sumValidationTest(md5.substring(0, 31), schemaType, true); // invalidate <32 chars
      runFileMd5sumValidationTest(
          md5.substring(0, 31) + "q", schemaType, true); // invalidate non-hex value
      runFileMd5sumValidationTest(md5, schemaType, false); // validate hex value with 32 chars
    }
  }

  private void runFileMd5sumValidationTest(String md5, String schemaType, boolean shouldBeError) {
    val testFileName = DEFAULT_TEST_FILE_MAP.get(schemaType);

    val payload = getJsonFile(testFileName);
    val fileNodes = newArrayList(payload.path("files"));
    assertFalse(fileNodes.isEmpty());
    for (val fileNode : fileNodes) {
      ((ObjectNode) fileNode).put("fileMd5sum", md5);
    }

    val results = service.validate(payload);

    if (shouldBeError) {
      assertTrue(results.isPresent());
      val errors = Splitters.COMMA.splitToList(results.get());
      errors.forEach(
          e -> assertTrue(e.matches("^#/files/[0|1]/fileMd5sum: string.*does not match pattern.*")));
    } else {
      assertFalse(
          format("Expecting validation not to have an error: %s", results.orElse(null)),
          results.isPresent());
    }
  }

  private JsonNode getJsonFile(String name) {
    return getJsonNodeFromClasspath("documents/validation/" + name);
  }
}
