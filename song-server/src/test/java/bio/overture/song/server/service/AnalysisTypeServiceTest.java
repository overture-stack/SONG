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
import bio.overture.song.server.controller.analysisType.AnalysisTypePageableResolver;
import bio.overture.song.server.model.dto.AnalysisType;
import bio.overture.song.server.model.entity.AnalysisSchema;
import bio.overture.song.server.model.projections.AnalysisSchemaNameProjection;
import bio.overture.song.server.repository.AnalysisSchemaRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.everit.json.schema.Schema;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.stream.IntStream;

import static java.util.Comparator.comparingInt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.emptyList;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_TYPE_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerErrors.MALFORMED_PARAMETER;
import static bio.overture.song.core.testing.SongErrorAssertions.assertSongError;
import static bio.overture.song.core.utils.JsonUtils.mapper;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.controller.analysisType.AnalysisTypePageableResolver.createAnalysisTypePageable;
import static bio.overture.song.server.controller.analysisType.AnalysisTypePageableResolver.createDefaultPageable;
import static bio.overture.song.server.controller.analysisType.AnalysisTypePageableResolver.parseAnalysisTypePageable;
import static bio.overture.song.server.service.AnalysisTypeService.buildAnalysisType;
import static bio.overture.song.server.service.AnalysisTypeService.resolveAnalysisTypeId;
import static bio.overture.song.server.utils.CollectionUtils.mapToImmutableSet;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class AnalysisTypeServiceTest {

  @Autowired private AnalysisTypeService analysisTypeService;
  @Autowired private AnalysisSchemaRepository analysisSchemaRepository;

  private RandomGenerator randomGenerator;

  @Before
  public void beforeTest() {
    this.randomGenerator = createRandomGenerator(getClass().getSimpleName());
  }

  @Test
  public void listAnalysisTypes_nonExisting_empty() {
    // Since the database could already contain elements from previous tests, the dao needs to be mocked
    val repo = mock(AnalysisSchemaRepository.class);
    when(repo.findDistinctBy(AnalysisSchemaNameProjection.class))
        .thenReturn(emptyList());
    val service = new AnalysisTypeService(() -> (Schema) null, repo);
    val pageable = AnalysisTypePageableResolver.createDefaultPageable();
    assertThat(service.listAllAnalysisTypes(pageable, false)).isEmpty();
  }

  @Test
  public void testRob(){
    val data = generateData(10);
    val pageable = createDefaultPageable();
    val actualNames = analysisTypeService.listAllAnalysisTypes(pageable, false);
    log.info("Sdf");

  }

  @Test
  @SneakyThrows
  public void listAnalysisTypes_multiVersionMultiAnalysisTypes_NonDuplicateListOfNames() {
    // Generate data
    val repeats = 5;
    val data = generateData(repeats);

    // Extract expected names
    val expectedNames = mapToImmutableSet(data, AnalysisType::getName);

    // Get actual names
    val dummyPageable = createAnalysisTypePageable(0, 1, DESC);
    val p = analysisTypeService.listAllAnalysisTypes(dummyPageable, true);
    val max = (int)p.getTotalElements();
    val pageable = createAnalysisTypePageable(0, max, DESC);
    val actualNames = analysisTypeService.listAllAnalysisTypes(pageable, true).stream()
        .map(AnalysisType::getName)
        .collect(toImmutableSet());

    // Assert actualNames contains all the expectedNames (actual names could have more elements due to previous tests)
    assertThat(actualNames).containsAll(expectedNames);
  }

  @Test
  public void getAnalysisType_analysisTypeDNE_notFound() {
    val nonExistingAnalysisTypeName = generateUniqueAnalysisTypeName();
    assertSongError(() -> analysisTypeService.getAnalysisType(resolveAnalysisTypeId(nonExistingAnalysisTypeName, 1)), ANALYSIS_TYPE_NOT_FOUND);
  }

  @Test
  public void getAnalysisType_malformedVersion_malformedParameter() {
    val repeats = 5;
    val data = generateData(repeats);
    val testName = data.get(data.size() - 1).getName();

    // test when version <= 0
    assertSongError(() -> analysisTypeService.getAnalysisType(resolveAnalysisTypeId(testName, 0)), MALFORMED_PARAMETER);
    assertSongError(() -> analysisTypeService.getAnalysisType(resolveAnalysisTypeId(testName, -1)), MALFORMED_PARAMETER);
  }

  @Test
  public void getAnalysisType_versionDNE_notFound() {
    val repeats = 5;
    val data = generateData(repeats);
    val testName = data.get(data.size() - 1).getName();

    // test when version > latest
    val analysisTypeId = resolveAnalysisTypeId(testName, repeats + 1);
    assertSongError(() -> analysisTypeService.getAnalysisType(analysisTypeId), ANALYSIS_TYPE_NOT_FOUND);
  }

  @Test
  public void getAnalysisType_multiNamesMultiVersions_success() {
    // Generate test data
    val repeats = 5;
    val version = repeats - 1;
    val data = generateData(repeats);
    val testName = data.get(data.size() - 1).getName();

    // Find all analysisTypes matching the name, sort descending by id and store as expectedAnalysisTypes
    val expectedAnalysisSchemasByName = analysisSchemaRepository.findAll().stream()
        .filter(x -> x.getName().equals(testName))
        .sorted((at1, at2) -> comparingInt(AnalysisSchema::getId).compare(at1, at2))
        .collect(toImmutableList());
    assertThat(expectedAnalysisSchemasByName).hasSize(repeats);

    // Get the expectedAnalysisType for the specified version
    val expectedAnalysisSchemaForVersion = expectedAnalysisSchemasByName.get(version - 1);
    val expectedAnalysisType = buildAnalysisType(testName, version,
        expectedAnalysisSchemaForVersion.getSchema());

    // Get the actual Schema for the specified version
    val analysisTypeId = resolveAnalysisTypeId(testName, version);
    val actualAnalysisType = analysisTypeService.getAnalysisType(analysisTypeId);

    // Assert the schemas match for the specified version
    assertThat(actualAnalysisType).isEqualTo(expectedAnalysisType);
  }

  @Test
  public void getLatestAnalysisType_multiNamesMultiVersions_success() {
    // Generate test data
    val repeats = 5;
    val latestVersion = repeats;
    val data = generateData(repeats);
    val testName = data.get(data.size() - 1).getName();

    // Find all analysisTypes matching the name, sort descending by id and store as expectedAnalysisTypes
    val expectedAnalysisSchemasByName = analysisSchemaRepository.findAll().stream()
        .filter(x -> x.getName().equals(testName))
        .sorted((at1, at2) -> comparingInt(AnalysisSchema::getId).compare(at1, at2))
        .collect(toImmutableList());
    assertThat(expectedAnalysisSchemasByName).hasSize(repeats);

    // Get the expectedLatestAnalysisType for the latest version
    val expectedLatestAnalysisSchema = expectedAnalysisSchemasByName.get(latestVersion - 1);
    val expectedLatestAnalysisType = buildAnalysisType(testName, latestVersion,
        expectedLatestAnalysisSchema.getSchema());

    // Get the actual Schema for the latest version
    val pageable = parseAnalysisTypePageable("0", "1",
        "version", "desc");

    val actualLatestAnalysisTypePage = analysisTypeService.listAnalysisTypesFilterNames(ImmutableList.of(testName),
        pageable, true);
    assertThat(actualLatestAnalysisTypePage.getContent()).isEqualTo(1);
    val actualLatestAnalysisType = actualLatestAnalysisTypePage.getContent().get(0);

    // Assert the schemas match for the latest version
    assertThat(actualLatestAnalysisType).isEqualTo(expectedLatestAnalysisType);
  }

  private List<AnalysisType> generateData(int repeats) {
    val names = IntStream.range(0, repeats)
        .boxed()
        .map(x -> "exampleAnalysisType-" + randomGenerator.generateRandomAsciiString(10))
        .collect(toImmutableList());

    return IntStream.range(0, repeats * repeats)
        .boxed()
        .map(i -> {
          val name = names.get(i % repeats);
          val schema = mapper().createObjectNode().put("$id", randomGenerator.generateRandomUUIDAsString());
          return analysisTypeService.register(name, schema);
        })
        .collect(toImmutableList());
  }

  private String generateUniqueAnalysisTypeName() {
    String analysisType = null;
    do {
      analysisType = randomGenerator.generateRandomAsciiString(10);
    } while (analysisSchemaRepository.countAllByName(analysisType) > 0);
    return analysisType;
  }

}