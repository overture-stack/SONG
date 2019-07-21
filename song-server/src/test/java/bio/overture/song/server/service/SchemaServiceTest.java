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

import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.repository.AnalysisTypeRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.everit.json.schema.Schema;
import org.icgc.dcc.common.core.json.JsonNodeBuilders;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.stream.IntStream;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.emptyList;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class SchemaServiceTest {


	@Autowired
	private SchemaService schemaService;

	@Test
	public void listAnalysisTypes_nonExisting_empty(){
		val repo = mock(AnalysisTypeRepository.class);
		when(repo.findDistinctBy(SchemaService.AnalysisTypeNameView.class))
				.thenReturn(emptyList());
		val service = new SchemaService(() -> (Schema)null, repo);
		assertThat(service.listAnalysisTypeNames()).isEmpty();
	}

	@Test
	@SneakyThrows
	public void listAnalysisTypes_multiVersionMultiAnalysisTypes_NonDuplicateListOfNames(){
	  // Generate 3 versions of 3 analysisTypes (all have the same schema)
		val randomGenerator = RandomGenerator.createRandomGenerator("nonDups");
		val schema = JsonNodeBuilders.object().end();
		val repeats = 3;
		val names = IntStream.range(0, repeats)
				.boxed()
				.map(x -> randomGenerator.generateRandomAsciiString(10) )
				.collect(toImmutableList());
		val versions = IntStream.range(0, repeats*repeats)
				.boxed()
				.map(i -> schemaService.commitAnalysisType(names.get(i%repeats), schema))
				.collect(toImmutableList());
		assertThat(names).hasSize(repeats);
		assertThat(versions).hasSize(repeats*repeats);
		assertThat(newHashSet(versions)).hasSize(repeats);

		val registeredAnalysisTypes = schemaService.listAnalysisTypeNames();
		// Assert all generated names are in the list
		assertThat(registeredAnalysisTypes).containsAll(names);

		// Assert list does not have duplicates
		assertThat(newHashSet(registeredAnalysisTypes)).hasSize(repeats);
	}

}
