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
package org.icgc.dcc.song.server.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.common.core.util.stream.Streams;
import org.junit.Test;

import java.io.InputStream;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;

@Slf4j
public class schemaValidationTests {

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
      assertThat(schema.has(PROPERTIES)).isTrue();
      val propertiesSchema = schema.path(PROPERTIES);
      assertThat(propertiesSchema.has(ANALYSIS_ID)).isTrue();
      val analysisIdSchema = propertiesSchema.path(ANALYSIS_ID);
      assertThat(analysisIdSchema.has(PATTERN)).isTrue();
      assertThat(getTypes(analysisIdSchema)).contains(STRING);
      assertThat(analysisIdSchema.path(PATTERN).textValue()).isEqualTo("^[a-zA-Z0-9]{1}[a-zA-Z0-9-_]{2,511}$");
    }
  }

  private static Set<String> getTypes(JsonNode node){
    assertThat(node.has(TYPE)).isTrue();
    return Streams.stream(node.path(TYPE).iterator())
        .filter(x -> !x.isArray())
        .map(JsonNode::textValue)
        .collect(toImmutableSet());
  }

  @Test
  public void validate_submit_sequencing_read_happy_path() throws Exception {
    val errors =
        validate("schemas/sequencingRead.json", "documents/sequencingread-valid.json");
    assertThat(errors.size()).isEqualTo(0);
  }

  @Test
  public void validate_submit_sequencing_read_missing_required() throws Exception {
    val errors = validate("schemas/sequencingRead.json",
            "documents/sequencingread-missing-required.json");
    assertThat(errors.size()).isEqualTo(4);
  }

  @Test
  public void validate_submit_sequencing_read_invalid_enum() throws Exception {
    val errors =
        validate("schemas/sequencingRead.json", "documents/sequencingread-invalid-enum.json");
    assertThat(errors.size()).isEqualTo(6);
  }

  @Test
  public void validate_submit_variant_call_happy_path() throws Exception {
    val errors = validate("schemas/variantCall.json", "documents/variantcall-valid.json");
    assertThat(errors.size()).isEqualTo(0);
  }

  @Test
  public void validate_submit_variant_call_missing_required() throws Exception {
    val errors =
        validate("schemas/variantCall.json", "documents/variantcall-missing-required.json");
    assertThat(errors.size()).isEqualTo(4);
  }

  @Test
  public void validate_submit_variant_call_invalid_enum() throws Exception {
    val errors =
        validate("schemas/variantCall.json", "documents/variantcall-invalid-enum.json");
    assertThat(errors.size()).isEqualTo(6);
  }

  protected Set<ValidationMessage> validate(String schemaFile, String documentFile) throws Exception {
    JsonSchema schema = getJsonSchemaFromClasspath(schemaFile);
    JsonNode node = getJsonNodeFromClasspath(documentFile);
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
    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
    JsonSchema schema = factory.getSchema(is);
    return schema;
  }

  protected JsonNode getJsonNodeFromClasspath(String name) throws Exception {
    InputStream is1 = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.readTree(is1);
    return node;
  }
}
