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

package bio.overture.song.server.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static net.javacrumbs.jsonunit.JsonAssert.when;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static bio.overture.song.server.model.enums.Constants.SEQUENCING_READ_TYPE;
import static bio.overture.song.server.model.enums.Constants.VARIANT_CALL_TYPE;
import static bio.overture.song.server.utils.TestFiles.getJsonNodeFromClasspath;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class SchemaServiceTest {

  @Autowired
  private SchemaService schemaService;

  @Autowired
  private Map<String, String> jsonSchemaMap;

  @Test
  public void testListSchemaIds(){
    val schemaIds = schemaService.listSchemaIds().getSchemaIds();
    assertThat(schemaIds, containsInAnyOrder(SEQUENCING_READ_TYPE, VARIANT_CALL_TYPE));
  }

  @Test
  public void testGetSchemaForSequencingRead(){
    runGetSchemaTest(SEQUENCING_READ_TYPE);
  }

  @Test
  public void testGetSchemaForVariantCall(){
    runGetSchemaTest(VARIANT_CALL_TYPE);
  }

  private void runGetSchemaTest(String analysisType){
    val resp = schemaService.getSchema(analysisType);
    assertEquals(resp.getSchemaId(),analysisType);

    val actual = resp.getJsonSchema();
    val expected = getJsonNodeFromClasspath(jsonSchemaMap.get(analysisType));
    assertJsonEquals(actual, expected, when(IGNORING_ARRAY_ORDER));
  }

}
