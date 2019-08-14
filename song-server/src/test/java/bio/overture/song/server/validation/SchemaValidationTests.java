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

import bio.overture.song.server.model.analysis.SequencingReadAnalysis;
import bio.overture.song.server.model.analysis.VariantCallAnalysis;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.common.core.util.stream.Streams;
import org.junit.Test;

import java.io.InputStream;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Thread.currentThread;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static bio.overture.song.core.utils.JsonUtils.readTree;
import static bio.overture.song.core.utils.JsonUtils.toJson;
import static bio.overture.song.server.utils.generator.PayloadGenerator.createPayloadGenerator;

@Slf4j
public class SchemaValidationTests {

  private static final String ANALYSIS_ID = "analysisId";
  private static final String PROPERTIES = "properties";
  private static final String TYPE = "type";
  private static final String STRING = "string";
  private static final String PATTERN = "pattern";

  @Test
  public void validate_analysis_id_regex() throws Exception {
    val schemaFiles = newArrayList("schemas/sequencingRead.json", "schemas/variantCall.json");
    for (val schemaFile : schemaFiles){
      val schema = getJsonNodeFromClasspath( schemaFile );
      assertTrue(schema.has(PROPERTIES));
      val propertiesSchema = schema.path(PROPERTIES);
      assertTrue(propertiesSchema.has(ANALYSIS_ID));
      val analysisIdSchema = propertiesSchema.path(ANALYSIS_ID);
      assertTrue(analysisIdSchema.has(PATTERN));
      assertThat(getTypes(analysisIdSchema), contains(STRING));
      assertEquals(analysisIdSchema.path(PATTERN).textValue(),"^[a-zA-Z0-9]{1}[a-zA-Z0-9-_]{1,34}[a-zA-Z0-9]{1}$");
    }
  }

  @Test
  public void validate_file_md5_regex() throws Exception {
    val schemaFiles = newArrayList("schemas/sequencingRead.json", "schemas/variantCall.json");
    for (val schemaFile : schemaFiles){
      val schema = getJsonNodeFromClasspath( schemaFile );
      val paths = Lists.newArrayList("properties", "file", "items", "properties", "fileMd5sum", "pattern");
      JsonNode currentNode = schema;
      for (val path : paths){
        assertTrue(currentNode.has(path));
        currentNode = currentNode.path(path);
      }
      assertEquals(currentNode.textValue(),"^[a-fA-F0-9]{32}$");
    }
  }

  private static Set<String> getTypes(JsonNode node){
    assertTrue(node.has(TYPE));
    return Streams.stream(node.path(TYPE).iterator())
        .filter(x -> !x.isArray())
        .map(JsonNode::textValue)
        .collect(toImmutableSet());
  }

  @Test
  public void validate_submit_sequencing_read_happy_path() throws Exception {
    val errors =
        validate("schemas/sequencingRead.json", "documents/sequencingread-valid.json");
    assertEquals(errors.size(),0);
  }

  @Test
  public void validate_submit_sequencing_read_missing_required() throws Exception {
    val errors = validate("schemas/sequencingRead.json",
            "documents/sequencingread-missing-required.json");
    assertEquals(errors.size(),4);
  }

  @Test
  public void validate_submit_sequencing_read_invalid_enum() throws Exception {
    val errors =
        validate("schemas/sequencingRead.json", "documents/sequencingread-invalid-enum.json");
    assertEquals(errors.size(),6);
  }

  @Test
  public void validate_submit_variant_call_happy_path() throws Exception {
    val errors = validate("schemas/variantCall.json", "documents/variantcall-valid.json");
    assertEquals(errors.size(),0);
  }

  @Test
  public void validate_submit_variant_call_missing_required() throws Exception {
    val errors =
        validate("schemas/variantCall.json", "documents/variantcall-missing-required.json");
    assertEquals(errors.size(),4);
  }

  @Test
  public void validate_submit_variant_call_invalid_enum() throws Exception {
    val errors =
        validate("schemas/variantCall.json", "documents/variantcall-invalid-enum.json");
    assertEquals(errors.size(),6);
  }


  @Test
  @SneakyThrows
  public void testFilenameValidation(){
    val payloadGenerator = createPayloadGenerator("FileValidationTest");
    val testMap = Maps.<String, Boolean>newHashMap();
    testMap.put( "myFile.(name)-some_cool[characters]", true);
    testMap.put("/myFile.(name)-some_cool[characters]",false);
    testMap.put("myFile.(name)-so/me_cool[characters]",false);
    testMap.put("myFile.(name)-so/me_co/ol[characters]",false);
    testMap.put("./myFile.(name)-so/me_co/ol[characters]",false);
    testMap.put("/",false);
    val analysesTypes = newArrayList(VariantCallAnalysis.class, SequencingReadAnalysis.class );
    for ( val analysisTypeClass : analysesTypes){
      String ext;
      String schemaFilename;
      String fixtureFilename;
      if (analysisTypeClass.equals(VariantCallAnalysis.class)) {
        ext = "vcf.gz";
        schemaFilename = "schemas/variantCall.json";
        fixtureFilename = "documents/variantcall-valid.json";
      } else {
        ext = "bam";
        schemaFilename = "schemas/sequencingRead.json";
        fixtureFilename = "documents/sequencingread-valid.json";
      }
      for (val entry : testMap.entrySet()){
        val filename = entry.getKey()+"."+ext;
        log.info("Testing Filename validation: '{}'", filename);
        val isGood = entry.getValue();
        val payload = payloadGenerator.generateRandomPayload(analysisTypeClass, fixtureFilename);
        payload.setAnalysisId(null);
        payload.getFile().get(0).setFileName(filename);
        val payloadNode = readTree(toJson(payload));
        val errors = validate(schemaFilename, payloadNode);

        if (isGood) {
          assertThat(errors, hasSize(0));
        } else {
          assertThat(errors, hasSize(1));
        }
      }
    }
  }

  protected Set<ValidationMessage> validate(String schemaFile, String documentFile) throws Exception {
    val node = getJsonNodeFromClasspath(documentFile);
    return validate(schemaFile, node);
  }

  protected Set<ValidationMessage> validate(String schemaFile, JsonNode node) throws Exception {
    val schema = getJsonSchemaFromClasspath(schemaFile);
    val errors = schema.validate(node);
    if (errors.size() > 0) {
      for (val msg : errors) {
        log.error(String.format("Error code %s: %s ", msg.getCode(), msg.getMessage()));
      }
    }
    return errors;
  }

  protected JsonSchema getJsonSchemaFromClasspath(String name) throws Exception {
    JsonSchemaFactory factory = new JsonSchemaFactory();
    InputStream is = currentThread().getContextClassLoader().getResourceAsStream(name);
    JsonSchema schema = factory.getSchema(is);
    return schema;
  }

  protected JsonNode getJsonNodeFromClasspath(String name) throws Exception {
    InputStream is1 = currentThread().getContextClassLoader().getResourceAsStream(name);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.readTree(is1);
    return node;
  }
}
