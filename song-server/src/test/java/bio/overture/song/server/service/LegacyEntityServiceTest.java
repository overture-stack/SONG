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

package bio.overture.song.server.service;

import static bio.overture.song.core.exceptions.ServerErrors.ILLEGAL_FILTER_PARAMETER;
import static bio.overture.song.core.exceptions.ServerErrors.ILLEGAL_QUERY_PARAMETER;
import static bio.overture.song.core.exceptions.ServerErrors.LEGACY_ENTITY_NOT_FOUND;
import static bio.overture.song.core.utils.JsonUtils.convertValue;
import static bio.overture.song.server.service.LegacyEntityService.createLegacyEntityService;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Math.pow;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Streams.stream;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import bio.overture.song.core.testing.SongErrorAssertions;
import bio.overture.song.server.converter.LegacyEntityConverter;
import bio.overture.song.server.model.legacy.LegacyDto;
import bio.overture.song.server.model.legacy.LegacyEntity;
import bio.overture.song.server.repository.LegacyEntityRepository;
import bio.overture.song.server.utils.ParameterChecker;
import bio.overture.song.server.utils.TestFiles;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class LegacyEntityServiceTest {

  private static LegacyEntityConverter CONVERTER = LegacyEntityConverter.INSTANCE;
  private static final Pageable DEFAULT_PAGEABLE = PageRequest.of(0, 2000);
  private static List<LegacyEntity> LEGACY_ENTITY_DATA;

  @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock private LegacyEntityRepository repository;

  private ParameterChecker parameterChecker;

  private LegacyEntityService service;

  private List<String> legacyEntityFieldNames;

  @BeforeClass
  public static void initClass() {
    val jsonData = TestFiles.getJsonNodeFromClasspath("documents/LegacyEntityData.json");
    LEGACY_ENTITY_DATA =
        stream(jsonData).map(x -> convertValue(x, LegacyEntity.class)).collect(toImmutableList());
  }

  @Before
  public void beforeTest() {
    setupRepositoryFindById();
    setupService();
    legacyEntityFieldNames = newArrayList(parameterChecker.getFieldNamesFor(LegacyEntity.class));
  }

  @Test
  public void testGetEntity() {
    val id = "FI1";
    val actualEntity = service.getEntity(id);
    val expectedEntity =
        LEGACY_ENTITY_DATA.stream()
            .filter(x -> x.getId().equals(id))
            .map(CONVERTER::convertToLegacyDto)
            .findFirst()
            .get();
    assertEquals(actualEntity, expectedEntity);
  }

  @Test
  public void testGetMissingEntity() {
    val id = "FI-DNE";
    SongErrorAssertions.assertSongError(() -> service.getEntity(id), LEGACY_ENTITY_NOT_FOUND);
  }

  @Test
  public void testFindByGnosId() {
    val gnosId = "AN1";
    Predicate<LegacyEntity> dbPredicate = x -> x.getGnosId().equals(gnosId);
    val params = createParams("gnosId", gnosId);
    val searchProbe = LegacyDto.builder().gnosId(gnosId).build();
    runTest(params, searchProbe, dbPredicate);
  }

  @Test
  public void testFindById() {
    val id = "FI1";
    Predicate<LegacyEntity> dbPredicate = x -> x.getId().equals(id);
    val params = createParams("id", id);
    val searchProbe = LegacyDto.builder().id(id).build();
    runTest(params, searchProbe, dbPredicate);
  }

  @Test
  public void testFindByAccess() {
    val access = "open";
    Predicate<LegacyEntity> dbPredicate = x -> x.getAccess().equals(access);
    val params = createParams("access", access);
    val searchProbe = LegacyDto.builder().access(access).build();
    runTest(params, searchProbe, dbPredicate);
  }

  @Test
  public void testFindByProjectCode() {
    val projectCode = "ST1";
    Predicate<LegacyEntity> dbPredicate = x -> x.getProjectCode().equals(projectCode);
    val params = createParams("projectCode", projectCode);
    val searchProbe = LegacyDto.builder().projectCode(projectCode).build();
    runTest(params, searchProbe, dbPredicate);
  }

  @Test
  public void testFindByFileName() {
    val fileName = "FN1.vcf.gz";
    Predicate<LegacyEntity> dbPredicate = x -> x.getProjectCode().equals(fileName);
    val params = createParams("fileName", fileName);
    val searchProbe = LegacyDto.builder().fileName(fileName).build();
    runTest(params, searchProbe, dbPredicate);
  }

  @Test
  public void testIllegalQuery() {
    // Validation should happen before searching, hence, we can use a dummy probe
    val dummyProbe = LegacyDto.builder().build();

    for (val fieldName : parameterChecker.getFieldNamesFor(LegacyDto.class)) {
      val params = createParams(format("%s_fake", fieldName), "anything");
      SongErrorAssertions.assertSongError(
          () -> service.find(params, dummyProbe, DEFAULT_PAGEABLE), ILLEGAL_QUERY_PARAMETER);
    }
  }

  @Test
  public void testIllegalFilter() {
    // Validation should happen before searching, hence, we can use a dummy probe
    val dummyProbe = LegacyDto.builder().build();

    for (val fieldName : parameterChecker.getFieldNamesFor(LegacyDto.class)) {
      val params = createFilterParams(newArrayList(format("%s_fake", fieldName)));
      SongErrorAssertions.assertSongError(
          () -> service.find(params, dummyProbe, DEFAULT_PAGEABLE), ILLEGAL_FILTER_PARAMETER);
    }
  }

  @Test
  public void testFiltering() {
    val numPermutations = 1 << legacyEntityFieldNames.size();
    val id = "FI1";
    val singleResultProbe = LegacyDto.builder().id(id).build();
    setupRepositoryFindAll(
        CONVERTER.convertToLegacyEntity(singleResultProbe), DEFAULT_PAGEABLE, x -> true);
    for (int p = 0; p < numPermutations; p++) {
      val params = createFilterParams(p);
      val fieldNames = params.get("fields");
      val response = service.find(params, singleResultProbe, DEFAULT_PAGEABLE);
      for (val node : response.path("content")) {
        if (p == 0) {
          assertEquals(node.size(), legacyEntityFieldNames.size());
        } else {
          assertEquals(node.size(), fieldNames.size());
        }
        for (val fieldName : fieldNames) {
          assertTrue(node.has(fieldName));
        }
      }
    }
  }

  private void runTest(
      MultiValueMap<String, String> params,
      LegacyDto searchProbe,
      Predicate<LegacyEntity> dbPredicate) {
    setupRepositoryFindAll(
        CONVERTER.convertToLegacyEntity(searchProbe), DEFAULT_PAGEABLE, dbPredicate);
    val response = service.find(params, searchProbe, DEFAULT_PAGEABLE);
    val actualDatas =
        stream(response.path("content"))
            .map(x -> convertValue(x, LegacyEntity.class))
            .collect(toImmutableList());
    val expectedDatas = LEGACY_ENTITY_DATA.stream().filter(dbPredicate).collect(toImmutableList());
    assertTrue(expectedDatas.containsAll(actualDatas));
    assertTrue(actualDatas.containsAll(expectedDatas));
  }

  private MultiValueMap<String, String> createParams(
      String queryField, String queryValue, String... filteredFields) {
    return createParams(queryField, queryValue, asList(filteredFields));
  }

  private MultiValueMap<String, String> createParams(
      String queryField, String queryValue, List<String> filteredFields) {
    val map = new LinkedMultiValueMap<String, String>();
    map.put(queryField, newArrayList(queryValue));
    map.put("fields", filteredFields);
    map.put("page", newArrayList(Integer.toString(DEFAULT_PAGEABLE.getPageNumber())));
    map.put("size", newArrayList(Integer.toString(DEFAULT_PAGEABLE.getPageSize())));
    return map;
  }

  private MultiValueMap<String, String> createFilterParams(int permutationNumber) {
    int size = legacyEntityFieldNames.size();
    assertThat(permutationNumber, lessThan((int) pow(2, size)));
    int pos = 0;
    val filteredFields = Lists.<String>newArrayList();
    for (val fieldName : legacyEntityFieldNames) {
      int mask = 1 << (size - pos - 1);
      val enableField = (permutationNumber & mask) > 0;
      if (enableField) {
        filteredFields.add(fieldName);
      }
      ++pos;
    }
    return createFilterParams(filteredFields);
  }

  private MultiValueMap<String, String> createFilterParams(List<String> filteredFields) {
    val map = new LinkedMultiValueMap<String, String>();
    map.put("fields", filteredFields);
    map.put("page", newArrayList(Integer.toString(DEFAULT_PAGEABLE.getPageNumber())));
    map.put("size", newArrayList(Integer.toString(DEFAULT_PAGEABLE.getPageSize())));
    return map;
  }

  private void setupRepositoryFindAll(
      LegacyEntity probe, Pageable pageable, Predicate<LegacyEntity> expectedFilter) {
    val entities = LEGACY_ENTITY_DATA.stream().filter(expectedFilter).collect(toImmutableList());
    when(repository.findAll(Example.of(probe), pageable))
        .thenReturn(new PageImpl<LegacyEntity>(entities, pageable, entities.size()));
  }

  private void setupService() {
    parameterChecker = ParameterChecker.createParameterChecker(LegacyEntity.class, LegacyDto.class);
    service = createLegacyEntityService(repository, parameterChecker, CONVERTER);
  }

  private void setupRepositoryFindById() {
    for (val entity : LEGACY_ENTITY_DATA) {
      when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
    }
  }
}
