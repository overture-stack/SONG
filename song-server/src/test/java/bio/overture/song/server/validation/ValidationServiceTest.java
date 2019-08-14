/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
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

import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.service.ValidationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.Map;
import java.util.function.Supplier;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.assertj.core.util.Lists.newArrayList;
import static org.icgc.dcc.common.core.util.Splitters.PIPE;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.utils.TestFiles.getJsonNodeFromClasspath;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class})
@ActiveProfiles("test")
public class ValidationServiceTest {

  private static final String SEQ_READ="SequencingRead";
  private static final String VAR_CALL="VariantCall";
  private static final String STUDY = "study";

  @Autowired
  private ValidationService service;

  @Test
  public void testValidateValidSequencingRead() {
    val payload=getJsonFile("sequencingRead.json").toString();
    val results=service.validate(payload,SEQ_READ);
    assertThat(results).isEmpty();
  }

  @Test
  public void testValidateValidSequencingReadWithArchive() {
    val payload=getJsonFile("sequencingReadWithArchive.json").toString();
    val results=service.validate(payload,SEQ_READ);
    assertThat(results).isEmpty();
  }

  @Test
  public void testValidateValidVariantCall() {
    val payload=getJsonFile("variantCall.json").toString();
    val results=service.validate(payload,VAR_CALL);
    assertThat(results).isEmpty();
  }

  @Test
  public void testValidateVariantCallMissingAnalysisType() {
    val payload=getJsonFile("variantCall.json");
    ((ObjectNode)payload).put("analysisType", (String)null);
    val results=service.validate(payload.toString(),VAR_CALL);
    assertThat(results).isNotEmpty();
    assertThat(results).hasValue("$.analysisType: does not have a value in the enumeration [variantCall]|$"
        + ".analysisType: null found, string expected");
  }

  @Test
  public void testValidateSequencingReadMissingAnalysisType() {
    val payload=getJsonFile("sequencingRead.json");
    ((ObjectNode)payload).put("analysisType", (String)null);
    val results=service.validate(payload.toString(),SEQ_READ);
    assertThat(results).isNotEmpty();
    assertThat(results).hasValue("$.analysisType: does not have a value in the enumeration [sequencingRead]|$"
        + ".analysisType: null found, string expected");
  }

  @Test
  public void testUnknownAnalysisTypeValidation() {
    val payload=getJsonFile("sequencingRead.json");
    val results=service.validate(payload.toString(), "SOME_UNKNOWN_ANALYSIS_TYPE");
    assertThat(results).isNotEmpty();
    assertThat(results).hasValue("Unknown processing problem: Internal Error: could not find specified schema uploadSOME_UNKNOWN_ANALYSIS_TYPE");
  }

  @Test
  public void testJsonParseError() {
    // Load json, and corrupt it
    val payload=getJsonFile("sequencingRead.json")
        .toString()
        .replaceFirst("\\{", "")
        .replaceFirst("\"", "");
    val results=service.validate(payload, SEQ_READ);
    assertThat(results).isNotEmpty();
    assertThat(results.get().contains("Invalid JSON document submitted:")).isTrue();
  }

  @Test
  public void testFileMd5Validation(){
    val testMap = Maps.<String, String>newHashMap();
    testMap.put(SEQ_READ, "sequencingRead.json");
    testMap.put(VAR_CALL, "variantCall.json");

    for (val testDataEntry : testMap.entrySet() ){
      val schemaType = testDataEntry.getKey();
      val testFileName = testDataEntry.getValue();

      val payload = getJsonFile(testFileName);
      val fileNodes = newArrayList(payload.path("file"));
      assertThat(fileNodes.size()).isGreaterThan(1);
      val fileNode0 = ((ObjectNode)fileNodes.get(0)).put("fileMd5sum", "q123"); // less than 32 and non-hex number
      val fileNode1 = ((ObjectNode)fileNodes.get(1)).put("fileMd5sum", "q0123456789012345678901234567890123456789"); //more than 32 and non-hex number

      val results = service.validate(payload.toString(), schemaType);

      assertThat(results).isNotEmpty();

      val errors = PIPE.splitToList(results.get());
      assertThat(errors).hasSize(2);
      for (val error : errors){
        assertThat(error).contains("fileMd5sum: does not match the regex pattern");
      }
    }

  }
  @Test
  public void testFileMd5sumValidation(){
    val md5 = randomGenerator.generateRandomMD5();
    assertEquals(md5.length(),32);
    for (val schemaType : DEFAULT_TEST_FILE_MAP.keySet()){
      runFileMd5sumValidationTest(md5+"1", schemaType, true); // invalidate >32 chars
      runFileMd5sumValidationTest(md5.substring(0,31), schemaType, true); // invalidate <32 chars
      runFileMd5sumValidationTest(md5.substring(0,31)+"q", schemaType, true); //invalidate non-hex value
      runFileMd5sumValidationTest(md5, schemaType, false); //validate hex value with 32 chars
    }
  }

  @Test
  public void testAnalysisIdValidation(){
    val array = new String[]{"_", "-"};
    for (val schemaType : DEFAULT_TEST_FILE_MAP.keySet()){
      runAnalysisIdValidationErrorTest(randomGenerator.generateRandomAsciiString(37), schemaType, true); // invalidate >36 chars
      for (val c : array){
        runAnalysisIdValidationErrorTest(c+randomGenerator.generateRandomAsciiString(35), schemaType, true); //invalidate char at beginning
        runAnalysisIdValidationErrorTest(c+randomGenerator.generateRandomAsciiString(36), schemaType, true); // invalidate >36 and char at begining
        runAnalysisIdValidationErrorTest(randomGenerator.generateRandomAsciiString(35)+c, schemaType, true); //invalidate char at end
        runAnalysisIdValidationErrorTest(randomGenerator.generateRandomAsciiString(36)+c, schemaType, true); // invalidate >36 and char at end
      }
      runAnalysisIdValidationErrorTest(randomGenerator.generateRandomAsciiString(1), schemaType, true);
      runAnalysisIdValidationErrorTest(randomGenerator.generateRandomAsciiString(2), schemaType, true);
      runAnalysisIdValidationErrorTest(randomGenerator.generateRandomAsciiString(3), schemaType, false);
      runAnalysisIdValidationErrorTest(randomGenerator.generateRandomAsciiString(36), schemaType, false);
    }
  }

  private void runRequiredStringForPayloadTest(ObjectNode payload, Supplier<ObjectNode> nodeGetter,
      String fieldName, String schemaType){
    val node = nodeGetter.get();
    node.put(fieldName, "");
    val emptyResults = service.validate(payload.toString(), schemaType);
    assertThat(emptyResults).isNotEmpty();
    assertThat(emptyResults.get()).endsWith(format("%s: must be at least 1 characters long", fieldName));

    node.put(fieldName, (String)null);
    val nullResults = service.validate(payload.toString(), schemaType);
    assertThat(nullResults).isNotEmpty();
    assertThat(nullResults.get()).endsWith(format("%s: null found, string expected", fieldName));

    node.remove(fieldName);
    val missingResults = service.validate(payload.toString(), schemaType);
    assertThat(missingResults).isNotEmpty();
    assertThat(missingResults.get()).endsWith(format("%s: is missing but it is required", fieldName));

    node.put(fieldName, randomGenerator.generateRandomAsciiString(randomGenerator.generateRandomIntRange(1,4)));
    val goodResults = service.validate(payload.toString(), schemaType);
    assertThat(goodResults).isEmpty();
  }

  private final RandomGenerator randomGenerator = createRandomGenerator(ValidationServiceTest.class.getSimpleName());

  private static final Map<String, String> DEFAULT_TEST_FILE_MAP = Maps.newHashMap();
  static {
    DEFAULT_TEST_FILE_MAP.put(SEQ_READ, "sequencingRead.json");
    DEFAULT_TEST_FILE_MAP.put(VAR_CALL, "variantCall.json");
  }

  private ObjectNode toObjectNode(String schemaType ){
    val testFileName = DEFAULT_TEST_FILE_MAP.get(schemaType);
    return (ObjectNode)getJsonFile(testFileName);
  }

  private void runAnalysisIdValidationErrorTest(String analysisId, String schemaType, boolean shouldBeError){
    val payload = toObjectNode(schemaType);
    payload.put("analysisId", analysisId);

    val results = service.validate(payload.toString(), schemaType);

    if (shouldBeError){
      assertThat(results).isNotEmpty();
      assertThat(results.get()).contains("analysisId: does not match the regex pattern");
    } else {
      assertThat(results).as("Expecting validation not to have an error").isEmpty();
    }
  }

  private void runFileMd5sumValidationTest(String md5, String schemaType, boolean shouldBeError){
    val testFileName = DEFAULT_TEST_FILE_MAP.get(schemaType);

    val payload = getJsonFile(testFileName);
    val fileNodes = newArrayList(payload.path("file"));
    assertThat(fileNodes).isNotEmpty();
    for (val fileNode : fileNodes){
      ((ObjectNode)fileNode).put("fileMd5sum", md5);
    }

    val results = service.validate(payload.toString(), schemaType);

    if (shouldBeError){
      assertThat(results).isNotEmpty();
      assertThat(results.get()).contains("fileMd5sum: does not match the regex pattern");
    } else {
      assertThat(results).as("Expecting validation not to have an error: %s", results.orElse(null)).isEmpty();
    }
  }

  private JsonNode getJsonFile(String name) {
    return getJsonNodeFromClasspath("documents/validation/" + name);
  }

}
