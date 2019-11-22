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

import bio.overture.song.core.model.AnalysisTypeId;
import bio.overture.song.server.service.AnalysisTypeService;
import bio.overture.song.server.utils.generator.LegacyAnalysisTypeName;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.assertj.core.util.Sets;
import org.everit.json.schema.ValidationException;
import org.icgc.dcc.common.core.util.Joiners;
import org.icgc.dcc.common.core.util.stream.Streams;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.InputStream;
import java.util.Optional;
import java.util.Set;

import static java.lang.Thread.currentThread;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static bio.overture.song.core.utils.JsonUtils.readTree;
import static bio.overture.song.core.utils.JsonUtils.toJson;
import static bio.overture.song.server.utils.JsonObjects.convertToJSONObject;
import static bio.overture.song.server.utils.JsonSchemas.buildSchema;
import static bio.overture.song.server.utils.generator.LegacyAnalysisTypeName.SEQUENCING_READ;
import static bio.overture.song.server.utils.generator.LegacyAnalysisTypeName.VARIANT_CALL;
import static bio.overture.song.server.utils.generator.PayloadGenerator.createPayloadGenerator;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class SchemaValidationTests {

  private static final String ANALYSIS_ID = "analysisId";
  private static final String PROPERTIES = "properties";
  private static final String TYPE = "type";
  private static final String STRING = "string";
  private static final String PATTERN = "pattern";

  @Autowired private AnalysisTypeService analysisTypeService;

  private Optional<JsonNode> getPath(JsonNode root, String... paths) {
    JsonNode currentNode = root;
    for (val path : paths) {
      if (!currentNode.hasNonNull(path)) {
        return Optional.empty();
      }
      currentNode = currentNode.path(path);
    }
    return Optional.of(currentNode);
  }

  @Test
  public void validate_file_md5_regex() {
    for (val legacyAnalysisTypeName : LegacyAnalysisTypeName.values()) {
      val schema = getLegacySchemaJson(legacyAnalysisTypeName);
      val ref = getPath(schema, "properties", "file", "items", "$ref");
      assertTrue(ref.isPresent());
      assertEquals(ref.get().textValue(), "#/definitions/file/fileData");

      val patternNode = getPath(schema, "definitions", "common", "md5", "pattern");
      assertTrue(patternNode.isPresent());
      assertEquals(patternNode.get().textValue(), "^[a-fA-F0-9]{32}$");
    }
  }

  @Test
  public void validate_submit_sequencing_read_happy_path() throws Exception {
    val errors = validate(SEQUENCING_READ, "documents/sequencingread-valid.json");
    assertEquals(errors.size(), 0);
  }

  @Test
  public void validate_submit_sequencing_read_missing_required() throws Exception {
    val errors = validate(SEQUENCING_READ, "documents/sequencingread-missing-required.json");
    assertEquals(errors.size(), 4);
  }

  @Test
  public void validate_submit_sequencing_read_invalid_enum() throws Exception {
    val errors = validate(SEQUENCING_READ, "documents/sequencingread-invalid-enum.json");
    assertEquals(errors.size(), 6);
  }

  @Test
  public void validate_submit_variant_call_happy_path() throws Exception {
    val errors = validate(VARIANT_CALL, "documents/variantcall-valid.json");
    assertEquals(errors.size(), 0);
  }

  @Test
  public void validate_submit_variant_call_missing_required() throws Exception {
    val errors = validate(VARIANT_CALL, "documents/variantcall-missing-required.json");
    assertEquals(errors.size(), 4);
  }

  @Test
  public void validate_submit_variant_call_invalid_enum() throws Exception {
    val errors = validate(VARIANT_CALL, "documents/variantcall-invalid-enum.json");
    assertEquals(errors.size(), 6);
  }

  @Test
  @SneakyThrows
  public void testFilenameValidation() {
    val payloadGenerator = createPayloadGenerator("FileValidationTest");
    val testMap = Maps.<String, Boolean>newHashMap();
    testMap.put("myFile.(name)-some_cool[characters]", true);
    testMap.put("/myFile.(name)-some_cool[characters]", false);
    testMap.put("myFile.(name)-so/me_cool[characters]", false);
    testMap.put("myFile.(name)-so/me_co/ol[characters]", false);
    testMap.put("./myFile.(name)-so/me_co/ol[characters]", false);
    testMap.put("/", false);
    for (val legacyAnalysisTypeName : LegacyAnalysisTypeName.values()) {
      String ext;
      String fixtureFilename;
      if (legacyAnalysisTypeName == VARIANT_CALL) {
        ext = "vcf.gz";
        fixtureFilename = "documents/variantcall-valid.json";
      } else {
        ext = "bam";
        fixtureFilename = "documents/sequencingread-valid.json";
      }
      for (val entry : testMap.entrySet()) {
        val filename = entry.getKey() + "." + ext;
        log.info("Testing Filename validation: '{}'", filename);
        val isGood = entry.getValue();
        val payload = payloadGenerator.generateRandomPayload(fixtureFilename);
        payload.getFile().get(0).setFileName(filename);
        val payloadNode = readTree(toJson(payload));
        val errors = validate(legacyAnalysisTypeName, payloadNode);

        if (isGood) {
          assertEquals(0, errors.size());
        } else {
          assertEquals(1, errors.size());
        }
      }
    }
  }

  protected Set<String> validate(
      LegacyAnalysisTypeName legacyAnalysisTypeName, String payloadFilename) throws Exception {
    val node = getJsonNodeFromClasspath(payloadFilename);
    return validate(legacyAnalysisTypeName, node);
  }

  protected Set<String> validate(
      LegacyAnalysisTypeName legacyAnalysisTypeName, JsonNode payloadJson) throws Exception {
    val schemaJson = getLegacySchemaJson(legacyAnalysisTypeName);
    val schema = buildSchema(schemaJson);
    val errors = Sets.<String>newHashSet();
    try {
      schema.validate(convertToJSONObject(payloadJson));
    } catch (ValidationException e) {
      log.error(String.format("Error: %s ", Joiners.COMMA.join(e.getAllMessages())));
      errors.addAll(e.getAllMessages());
    }
    return errors;
  }

  protected JsonNode getJsonNodeFromClasspath(String name) throws Exception {
    InputStream is1 = currentThread().getContextClassLoader().getResourceAsStream(name);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.readTree(is1);
    return node;
  }

  private JsonNode getLegacySchemaJson(LegacyAnalysisTypeName legacyAnalysisTypeName) {
    val aid =
        AnalysisTypeId.builder()
            .name(legacyAnalysisTypeName.getAnalysisTypeName())
            .version(1)
            .build();
    return analysisTypeService.getAnalysisType(aid, false).getSchema();
  }

  private static Set<String> getTypes(JsonNode node) {
    assertTrue(node.has(TYPE));
    return Streams.stream(node.path(TYPE).iterator())
        .filter(x -> !x.isArray())
        .map(JsonNode::textValue)
        .collect(toImmutableSet());
  }
}
