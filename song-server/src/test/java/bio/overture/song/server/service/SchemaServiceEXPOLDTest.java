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

import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.server.model.ExperimentSchema;
import bio.overture.song.server.repository.SchemaRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.common.core.json.JsonNodeBuilders;
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
import static org.assertj.core.api.Assertions.assertThat;
import static bio.overture.song.core.utils.JsonUtils.fromJson;
import static bio.overture.song.server.model.enums.Constants.SEQUENCING_READ_TYPE;
import static bio.overture.song.server.model.enums.Constants.VARIANT_CALL_TYPE;
import static bio.overture.song.server.utils.TestFiles.getJsonNodeFromClasspath;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class SchemaServiceEXPOLDTest {

	@Autowired
	private SchemaServiceOLD schemaServiceOLD;

	@Autowired
	private SchemaRepository schemaRepository;

	@Autowired
	private Map<String, String> jsonSchemaMap;

	@Test
	public void testListSchemaIds(){
		val schemaIds = schemaServiceOLD.listSchemaIds().getSchemaIds();
		assertThat(schemaIds).containsExactlyInAnyOrder(SEQUENCING_READ_TYPE, VARIANT_CALL_TYPE);
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
		val resp = schemaServiceOLD.getSchema(analysisType);
		assertThat(resp.getSchemaId()).isEqualTo(analysisType);

		val actual = resp.getJsonSchema();
		val expected = getJsonNodeFromClasspath(jsonSchemaMap.get(analysisType));
		assertJsonEquals(actual, expected, when(IGNORING_ARRAY_ORDER));
	}

	@Test
	public void testSchemaSave(){

    val schema = JsonNodeBuilders.object()
        .with("firstName", "Robert")
        .with("lastName", "Tisma")
        .with("age", 31)
        .end();

    val schemaEntity = ExperimentSchema.builder()
        .analysisType("robSchema")
        .schema(fromJson(schema, Map.class))
        .build();

    schemaRepository.save(schemaEntity);

    val result =  schemaRepository.findById("robSchema");
    assertThat(result).isNotEmpty();

    val readSchema = result.get().getSchema();
    assertThat(JsonUtils.toJsonNode(readSchema)).isEqualTo(schema);

  }

}
