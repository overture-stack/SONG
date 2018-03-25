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
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.val;
import org.icgc.dcc.song.server.service.ValidationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.server.utils.TestFiles.getJsonNodeFromClasspath;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class})
@ActiveProfiles("dev")
public class ValidationServiceTest {
  private static final String SEQ_READ="SequencingRead";
  private static final String VAR_CALL="VariantCall";

  @Autowired
  private ValidationService service;

  @Test
  public void testValidateValidSequencingRead() {
    val payload=getJsonFile("sequencingRead.json").toString();
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
  public void testValidateSequencingReadWithStudy() {
    val payload=getJsonFile("sequencingReadStudy.json").toString();
    val results=service.validate(payload,SEQ_READ);
    assertThat(results).isNotEmpty();
    assertThat(results).hasValue("Uploaded JSON document must not contain a study field");
  }

  @Test
  public void testValidateVariantCallWithStudy() {
    val payload=getJsonFile("variantCallStudy.json").toString();
    val results=service.validate(payload,VAR_CALL);
    assertThat(results).isNotEmpty();
    assertThat(results).hasValue("Uploaded JSON document must not contain a study field");
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

  private JsonNode getJsonFile(String name) {
    return getJsonNodeFromClasspath("documents/validation/" + name);
  }

}
