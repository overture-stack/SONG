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
import bio.overture.song.server.model.dto.GetAnalysisTypeResponse;
import bio.overture.song.server.model.dto.RegisterAnalysisTypeResponse;
import bio.overture.song.server.model.entity.AnalysisType;
import bio.overture.song.server.repository.AnalysisTypeRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.everit.json.schema.Schema;
import org.icgc.dcc.common.core.json.JsonNodeBuilders;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.IntStream;

import static java.util.Comparator.comparingInt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.emptyList;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_TYPE_NOT_FOUND;
import static bio.overture.song.core.testing.SongErrorAssertions.assertSongError;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.utils.CollectionUtils.mapToImmutableSet;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class SchemaServiceTest {

	@Autowired private SchemaService schemaService;
	@Autowired private AnalysisTypeRepository analysisTypeRepository;

	private RandomGenerator randomGenerator;

	@Before
	public void beforeTest(){
		this.randomGenerator = createRandomGenerator(getClass().getSimpleName());
	}

	@Test
	public void listAnalysisTypes_nonExisting_empty(){
	  // Since the database could already contain elements from previous tests, the dao needs to be mocked
		val repo = mock(AnalysisTypeRepository.class);
		when(repo.findDistinctBy(SchemaService.AnalysisTypeNameView.class))
				.thenReturn(emptyList());
		val service = new SchemaService(() -> (Schema)null, repo);
		assertThat(service.listAnalysisTypeNames()).isEmpty();
	}

	@Test
	@SneakyThrows
	public void listAnalysisTypes_multiVersionMultiAnalysisTypes_NonDuplicateListOfNames(){
	  // Generate data
		val repeats = 3;
		val data = generateData(repeats);

		// Extract expected names
		val expectedNames = mapToImmutableSet(data, RegisterAnalysisTypeResponse::getName);

		// Get actual names
		val actualNames = schemaService.listAnalysisTypeNames();

		// Assert actualNames contains all the expectedNames (actual names could have more elements due to previous tests)
		assertThat(actualNames).containsAll(expectedNames);
	}

	@Test
	public void getSchema_analysisTypeDNE_notFound(){
	  val nonExistingAnalysisType = generateUniqueAnalysisTypeName();
		assertSongError(() -> schemaService.getSchema(nonExistingAnalysisType, 1), ANALYSIS_TYPE_NOT_FOUND);
	}

	@Test
	public void getSchema_versionDNE_notFound(){
	  val repeats = 3;
		val data = generateData(repeats);
		val testName = data.get(data.size()-1).getName();

		// test when version <= 0
		assertSongError(() ->  schemaService.getSchema(testName, 0), ANALYSIS_TYPE_NOT_FOUND);
		assertSongError(() ->  schemaService.getSchema(testName, -1), ANALYSIS_TYPE_NOT_FOUND);

	  // test when version > latest
		assertSongError(() ->  schemaService.getSchema(testName, repeats+1), ANALYSIS_TYPE_NOT_FOUND);
  }

  @Test
	public void getSchema_multiNamesMultiVersions_Success(){
	  // Generate test data
		val repeats = 3;
		val version = repeats-1;
		val data = generateData(repeats);
		val testName = data.get(data.size()-1).getName();

		// Find all analysisTypes matching the name, sort descending by id and store as expectedAnalysisTypes
		val expectedAnalysisTypesForName = analysisTypeRepository.findAll().stream()
				.filter(x -> x.getName().equals(testName))
        .sorted((at1, at2) ->  comparingInt(AnalysisType::getId).compare(at1, at2))
				.collect(toImmutableList());
		assertThat(expectedAnalysisTypesForName).hasSize(repeats);

		// Get the expectedAnalysisType
		val expectedAnalysisTypeForVersion = expectedAnalysisTypesForName.get(version-1);
		val expectedAnalysisType = GetAnalysisTypeResponse.builder()
				.name(testName)
				.version(version)
				.schema( expectedAnalysisTypeForVersion.getSchema())
				.build();

		// Get the actual Schema
		val actualSchema = schemaService.getSchema(testName, version);

		// Assert the schemas match
		assertThat(actualSchema).isEqualTo(expectedAnalysisType);
	}

	private List<RegisterAnalysisTypeResponse> generateData(int repeats){
		val names = IntStream.range(0, repeats)
				.boxed()
				.map(x -> "exampleAnalysisType-"+randomGenerator.generateRandomAsciiString(10) )
				.collect(toImmutableList());

		return IntStream.range(0, repeats*repeats)
				.boxed()
				.map(i -> {
					val name = names.get(i%repeats);
					val schema = JsonNodeBuilders.object().with("$id", randomGenerator.generateRandomUUIDAsString()).end();
					val version = schemaService.commitAnalysisType(name, schema);
					return RegisterAnalysisTypeResponse.builder()
							.name(name)
							.version(version)
							.build();
				})
				.collect(toImmutableList());
	}

	private String generateUniqueAnalysisTypeName(){
		String analysisType = null;
		do{
			analysisType = randomGenerator.generateRandomAsciiString(10);
		} while (analysisTypeRepository.countAllByName(analysisType) > 0);
		return analysisType;
	}

}
