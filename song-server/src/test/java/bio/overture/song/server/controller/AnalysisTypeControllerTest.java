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

package bio.overture.song.server.controller;

import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_TYPE_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerErrors.MALFORMED_JSON_SCHEMA;
import static bio.overture.song.core.exceptions.ServerErrors.MALFORMED_PARAMETER;
import static bio.overture.song.core.exceptions.ServerErrors.SCHEMA_VIOLATION;
import static bio.overture.song.core.exceptions.SongError.parseErrorResponse;
import static bio.overture.song.core.utils.CollectionUtils.mapToImmutableSet;
import static bio.overture.song.core.utils.JsonUtils.mapper;
import static bio.overture.song.core.utils.JsonUtils.readTree;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.core.utils.RandomGenerator.randomList;
import static bio.overture.song.core.utils.RandomGenerator.randomStream;
import static bio.overture.song.core.utils.ResourceFetcher.ResourceType.MAIN;
import static bio.overture.song.server.controller.analysisType.AnalysisTypePageableResolver.DEFAULT_LIMIT;
import static bio.overture.song.server.utils.EndpointTester.createEndpointTester;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static net.javacrumbs.jsonunit.JsonAssert.when;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import bio.overture.song.core.exceptions.ServerError;
import bio.overture.song.core.model.AnalysisType;
import bio.overture.song.core.model.AnalysisTypeId;
import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.core.utils.ResourceFetcher;
import bio.overture.song.server.model.dto.schema.RegisterAnalysisTypeRequest;
import bio.overture.song.server.repository.AnalysisSchemaRepository;
import bio.overture.song.server.service.AnalysisTypeService;
import bio.overture.song.server.utils.EndpointTester;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.transaction.Transactional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.javacrumbs.jsonunit.core.Configuration;
import org.everit.json.schema.Schema;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc(secure = false)
@ActiveProfiles({"test"})
public class AnalysisTypeControllerTest {
  private static final boolean ENABLE_HTTP_LOGGING = false;

  // This was done because the autowired mockMvc wasn't working properly, it was getting http 403
  // errors
  @Autowired private WebApplicationContext webApplicationContext;

  @Autowired private Supplier<Schema> analysisTypeMetaSchemaSupplier;

  @Autowired private AnalysisTypeService analysisTypeService;

  @Autowired private AnalysisSchemaRepository analysisSchemaRepository;

  private MockMvc mockMvc;
  private EndpointTester endpointTester;
  private RandomGenerator randomGenerator;

  @Before
  public void beforeTest() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    this.endpointTester = createEndpointTester(mockMvc, ENABLE_HTTP_LOGGING);
    this.randomGenerator = createRandomGenerator(getClass().getCanonicalName());
  }

  /**
   * Unhappy Path: test a schema violation server error is returned when trying to register an
   * invalid schema with a valid name
   */
  @Test
  public void register_invalidSchema_schemaViolation() {
    // Generate test data
    val nonExistingName = generateUniqueName();

    // Generate valid schema
    val invalidSchema = mapper().createObjectNode();
    invalidSchema.putObject("$id").put("someKey", "someValue");

    // Assert registration of malformedNames results in MALFORMED_PARAMETER error
    val request =
        RegisterAnalysisTypeRequest.builder().name(nonExistingName).schema(invalidSchema).build();
    endpointTester.registerAnalysisTypePostRequestAnd(request).assertServerError(SCHEMA_VIOLATION);
  }

  /**
   * Unhappy Path: test a malformed parameter error is returned when trying to register a valid
   * schema with a malformed name
   */
  @Test
  public void register_malformedName_malformedParameter() {
    // Generate test data
    val p = randomGenerator.generateRandomAsciiString(10);
    val s = randomGenerator.generateRandomAsciiString(10);
    val malformedNames =
        newHashSet(
            p + "$" + s,
            p + ">" + s,
            p + " " + s,
            p + "," + s,
            p + ":" + s,
            " " + p + s,
            p + s + " ",
            " " + p + s + " ");

    // Generate valid schema
    val schema =
        mapper().createObjectNode().put("$id", randomGenerator.generateRandomUUIDAsString());

    // Assert registration of malformedNames results in MALFORMED_PARAMETER error
    malformedNames.forEach(
        name -> {
          val request = RegisterAnalysisTypeRequest.builder().name(name).schema(schema).build();
          endpointTester
              .registerAnalysisTypePostRequestAnd(request)
              .assertServerError(MALFORMED_PARAMETER);
        });
  }

  @Test
  public void schemaRendering_allPermutations_success() {
    val expectedUnrenderedJson = FETCHER.readJsonNode("unrendered-schema.json");
    val expectedRenderedJson = FETCHER.readJsonNode("rendered-schema.json");
    assertNull(analysisTypeService.resolveSchemaJsonView(expectedUnrenderedJson, true, true));
    assertEquals(
        analysisTypeService.resolveSchemaJsonView(expectedUnrenderedJson, true, false),
        expectedUnrenderedJson);
    assertNull(analysisTypeService.resolveSchemaJsonView(expectedUnrenderedJson, false, true));
    assertJsonEquals(
        expectedRenderedJson,
        analysisTypeService.resolveSchemaJsonView(expectedUnrenderedJson, false, false),
        Configuration.empty().withOptions(IGNORING_ARRAY_ORDER));
  }

  /**
   * Happy Path: Test successful initial registration of an analysisType as well as an update,
   * resulting in 2 versions
   */
  @Test
  @Transactional
  public void register_multipleNonExistingName_success() {
    // Generate unique name and schema, and create request
    val nonExistingName1 = generateUniqueName();
    val nonExistingName2 = generateUniqueName();
    assertNotEquals(nonExistingName1, nonExistingName2);

    val createSchema1 = generateRandomRegistrationPayload(randomGenerator);
    val createSchema2 = generateRandomRegistrationPayload(randomGenerator);
    val createRequest1 =
        RegisterAnalysisTypeRequest.builder().name(nonExistingName1).schema(createSchema1).build();
    val createRequest2 =
        RegisterAnalysisTypeRequest.builder().name(nonExistingName2).schema(createSchema2).build();

    val expectedCreateSchema1 =
        analysisTypeService.resolveSchemaJsonView(createSchema1, false, false);
    val expectedCreateSchema2 =
        analysisTypeService.resolveSchemaJsonView(createSchema2, false, false);

    // Build the expected AnalysisType using the AnalysisTypeService and also verify proper format
    val expectedAnalysisType1 =
        AnalysisType.builder()
            .name(nonExistingName1)
            .version(1)
            .schema(expectedCreateSchema1)
            .build();
    val expectedAnalysisType2 =
        AnalysisType.builder()
            .name(nonExistingName2)
            .version(1)
            .schema(expectedCreateSchema2)
            .build();

    // Assert the schema and name were properly registered
    endpointTester
        .registerAnalysisTypePostRequestAnd(createRequest1)
        .assertOneEntityEquals(expectedAnalysisType1);

    endpointTester
        .registerAnalysisTypePostRequestAnd(createRequest2)
        .assertOneEntityEquals(expectedAnalysisType2);

    // Update the schema for the same analysisTypeName
    val updateSchema1 = generateRandomRegistrationPayload(randomGenerator);
    val updateRequest1 =
        RegisterAnalysisTypeRequest.builder().name(nonExistingName1).schema(updateSchema1).build();
    val expectedUpdateSchema1 =
        analysisTypeService.resolveSchemaJsonView(updateSchema1, false, false);
    val expectedAnalysisTypeUpdate1 =
        AnalysisType.builder()
            .name(nonExistingName1)
            .version(2)
            .schema(expectedUpdateSchema1)
            .build();

    // Assert the schema and name were properly registered
    endpointTester
        .registerAnalysisTypePostRequestAnd(updateRequest1)
        .assertOneEntityEquals(expectedAnalysisTypeUpdate1);

    val updateSchema2 = generateRandomRegistrationPayload(randomGenerator);
    val updateRequest2 =
        RegisterAnalysisTypeRequest.builder().name(nonExistingName2).schema(updateSchema2).build();
    val expectedUpdateSchema2 =
        analysisTypeService.resolveSchemaJsonView(updateSchema2, false, false);
    val expectedAnalysisTypeUpdate2 =
        AnalysisType.builder()
            .name(nonExistingName2)
            .version(2)
            .schema(expectedUpdateSchema2)
            .build();

    // Assert the schema and name were properly registered
    endpointTester
        .registerAnalysisTypePostRequestAnd(updateRequest2)
        .assertOneEntityEquals(expectedAnalysisTypeUpdate2);

    // Assert there are only 2 entries for the analysisType name
    val results =
        endpointTester
            .listSchemasGetRequestAnd(
                ImmutableList.of(nonExistingName1, nonExistingName2),
                null,
                null,
                0,
                100,
                null,
                null)
            .extractPageResults(AnalysisType.class);
    val actualNames = mapToImmutableSet(results, AnalysisType::getName);
    assertEquals(actualNames.size(), 2);
    assertTrue(actualNames.containsAll(newHashSet(nonExistingName1, nonExistingName2)));
  }

  /**
   * Happy Path: Test the latest analysisType can be read when the version param is not defined
   * (missing)
   */
  @Test
  @Transactional
  public void getLatestAnalysisType_existing_success() {
    // Generate data
    val data = generateData(10);
    val expectedAnalysisTypeName = randomGenerator.randomElement(data).getName();

    // Get the latest version of the analysisType
    val expectedAnalysisType =
        data.stream()
            .filter(x -> x.getName().equals(expectedAnalysisTypeName))
            .filter(x -> x.getVersion().equals(10))
            .findFirst()
            .get();

    // Assert the response is the latest analysisType
    endpointTester
        .getLatestAnalysisTypeGetRequestAnd(expectedAnalysisType.getName())
        .assertOneEntityEquals(expectedAnalysisType);
  }

  /**
   * Happy Path: Test an analysisType can be read by requesting it by an analysisTypeId in the form
   * <name>:<version>
   */
  @Test
  @Transactional
  public void getAnalysisTypeByVersion_existing_success() {
    // Generate data
    val data = generateData(10);
    val expectedAnalysisType = randomGenerator.randomElement(data);

    // Assert the actual retrieved resource matches the expected
    endpointTester
        .getAnalysisTypeVersionGetRequestAnd(
            expectedAnalysisType.getName(), expectedAnalysisType.getVersion(), false)
        .assertOneEntityEquals(expectedAnalysisType);
  }

  /** Unhappy Path: test that malformed analysisTypeIds return a malformedParameter */
  @Test
  public void getAnalysisTypeByVersion_malformedId_malformedParameter() {
    val malformedIds =
        newHashSet(
            AnalysisTypeId.builder().name("som3th!ng$").version(4).build(),
            AnalysisTypeId.builder().name("something").version(-7).build(),
            AnalysisTypeId.builder().name("something").version(0).build());
    malformedIds.forEach(
        analysisTypeId ->
            endpointTester
                .getAnalysisTypeVersionGetRequestAnd(
                    analysisTypeId.getName(), analysisTypeId.getVersion(), false)
                .assertServerError(MALFORMED_PARAMETER));
  }

  /**
   * Unhappy Path: test that a NOT_FOUND status code is returned when retrieving a non-existent
   * analysisType name
   */
  @Test
  public void getAnalysisTypeByVersion_nonExistingName_notFound() {
    val nonExistingName = generateUniqueName();
    endpointTester
        .getAnalysisTypeVersionGetRequestAnd(nonExistingName, 1, false)
        .assertServerError(ANALYSIS_TYPE_NOT_FOUND);
  }

  /**
   * Unhappy Path: test that a NOT_FOUND status code is returned when retrieving a non-existent
   * analysisType version for an existing name
   */
  @Test
  @Transactional
  public void getAnalysisTypeByVersion_nonExistingVersion_notFound() {
    val existingAnalysisType = generateData(1).get(0);
    val nonExistingVersion = existingAnalysisType.getVersion() + 1;
    endpointTester
        .getAnalysisTypeVersionGetRequestAnd(
            existingAnalysisType.getName(), nonExistingVersion, false)
        .assertServerError(ANALYSIS_TYPE_NOT_FOUND);
  }

  /** Test that the meta schema can be requested */
  @Test
  @SneakyThrows
  public void getMetaSchema_existing_success() {
    // Expected meta schema json
    val expected = readTree(analysisTypeMetaSchemaSupplier.get().toString());

    // Assert the actual retrieved resource matches the expected
    val actual = endpointTester.getMetaSchemaGetRequestAnd().extractOneEntity(JsonNode.class);
    assertJsonEquals(expected, actual, when(IGNORING_ARRAY_ORDER));
  }

  /** Assert the flyway migration for legacy variantCall analysisType was run */
  @Test
  @SneakyThrows
  public void getLegacyVariantCall_existing_success() {
    runLegacyAnalysisTypeTest("variantCall");
  }

  /** Assert the flyway migration for legacy sequencingRead analysisType was run */
  @Test
  @SneakyThrows
  public void getLegacySequencingRead_existing_success() {
    runLegacyAnalysisTypeTest("sequencingRead");
  }

  /** Test the default size is DEFAULT_LIMIT */
  @Test
  public void listAnalysisTypes_hideSchema_success() {
    // Generate data
    val data = generateData(1);
    val expectedAnalysisType = data.stream().findAny().get();
    val name = expectedAnalysisType.getName();
    val actualAnalysisTypes =
        endpointTester
            .listSchemasGetRequestAnd(ImmutableList.of(name), null, false, null, null, null, null)
            .extractPageResults(AnalysisType.class);

    assertEquals(1, actualAnalysisTypes.size());
    val at = actualAnalysisTypes.get(0);
    assertNotNull(at.getSchema());
    assertEquals(at, expectedAnalysisType);

    val actualAnalysisTypes2 =
        endpointTester
            .listSchemasGetRequestAnd(ImmutableList.of(name), null, true, null, null, null, null)
            .extractPageResults(AnalysisType.class);

    assertEquals(1, actualAnalysisTypes2.size());
    val at2 = actualAnalysisTypes2.get(0);
    assertNull(at2.getSchema());
    assertEquals(at2.getName(), expectedAnalysisType.getName());
    assertEquals(at2.getVersion(), expectedAnalysisType.getVersion());
  }

  /** Test the default size is DEFAULT_LIMIT */
  @Test
  public void listAnalysisTypes_defaults_success() {
    // Generate data
    generateData(10);
    val actualAnalysisTypes =
        endpointTester
            .listSchemasGetRequestAnd(null, null, null, null, null, null, null)
            .extractPageResults(AnalysisType.class);

    // Assert default size is DEFAULT_LIMIT
    assertEquals(DEFAULT_LIMIT, 20);
    assertEquals(actualAnalysisTypes.size(), DEFAULT_LIMIT);
  }

  @Test
  @Transactional
  public void listAnalysisTypes_filterByMultiNamesVersions_success() {
    // Generate data
    val repeats = 10;
    val numSelectedNames = 3;
    val numSelectedVersions = 4;
    val data = generateData(repeats);
    val selectedNames =
        ImmutableSet.copyOf(
            randomGenerator.randomSublist(
                newArrayList(mapToImmutableSet(data, AnalysisType::getName)), numSelectedNames));
    val selectedVersions =
        ImmutableSet.copyOf(
            randomGenerator.randomSublist(
                range(1, repeats + 1).boxed().collect(toImmutableList()), numSelectedVersions));
    val selectedData =
        data.stream()
            .filter(
                x ->
                    selectedNames.contains(x.getName())
                        && selectedVersions.contains(x.getVersion()))
            .collect(toImmutableSet());

    // ********************************
    // All selected names and versions
    // ********************************
    val actualAllAnalysisTypes =
        endpointTester
            .listSchemasGetRequestAnd(
                selectedNames, selectedVersions, null, 0, selectedData.size() * 2, null, null)
            .extractPageResults(AnalysisType.class);
    assertEquals(actualAllAnalysisTypes.size(), selectedData.size());
    assertTrue(selectedData.containsAll(actualAllAnalysisTypes));

    // ********************************
    // Some selected names and versions
    // ********************************
    val someSelectedNames = newHashSet(selectedNames);
    someSelectedNames.add(generateUniqueName());
    someSelectedNames.add(generateUniqueName());

    val someSelectedVersions = newHashSet(selectedVersions);
    someSelectedVersions.add(repeats + 3);
    someSelectedVersions.add(repeats + 7);

    val actualSomeAnalysisTypes =
        endpointTester
            .listSchemasGetRequestAnd(
                someSelectedNames,
                someSelectedVersions,
                null,
                0,
                selectedData.size() * 2,
                null,
                null)
            .extractPageResults(AnalysisType.class);
    assertEquals(actualSomeAnalysisTypes.size(), selectedData.size());
    assertTrue(selectedData.containsAll(actualSomeAnalysisTypes));

    // ********************************
    // No selected names and versions
    // ********************************
    val noSelectedNames = newHashSet(randomList(this::generateUniqueName, 10));
    val noSelectedVersions = newHashSet(repeats + 3, repeats + 7);

    val actualNoAnalysisTypes =
        endpointTester
            .listSchemasGetRequestAnd(
                noSelectedNames, noSelectedVersions, null, 0, selectedData.size() * 2, null, null)
            .extractPageResults(AnalysisType.class);
    assertTrue(actualNoAnalysisTypes.isEmpty());

    // ********************************
    // Existing name, non-existing version
    // ********************************
    val actualNoAnalysisTypes2 =
        endpointTester
            .listSchemasGetRequestAnd(
                selectedNames, noSelectedVersions, null, 0, selectedData.size() * 2, null, null)
            .extractPageResults(AnalysisType.class);
    assertTrue(actualNoAnalysisTypes2.isEmpty());

    // ********************************
    // Non-existing name, existing versions
    // ********************************
    val actualNoAnalysisTypes3 =
        endpointTester
            .listSchemasGetRequestAnd(
                noSelectedNames, selectedVersions, null, 0, selectedData.size() * 2, null, null)
            .extractPageResults(AnalysisType.class);
    assertTrue(actualNoAnalysisTypes3.isEmpty());
  }

  /** Happy Path: test filtering the listing endpoint by multiple names only */
  @Test
  @Transactional
  public void listAnalysisTypes_filterByMultipleNames_success() {
    // Generate data
    val repeats = 10;
    val data = generateData(repeats);
    val names = ImmutableList.copyOf(mapToImmutableSet(data, AnalysisType::getName));

    val expectedNames = ImmutableSet.copyOf(randomGenerator.randomSublist(names, names.size() / 2));
    val expectedAnalysisTypes =
        data.stream().filter(x -> expectedNames.contains(x.getName())).collect(toList());

    // All Existing Names
    val actualAllAnalysisTypes =
        endpointTester
            .listSchemasGetRequestAnd(
                expectedNames, null, null, 0, expectedAnalysisTypes.size() * 2, null, null)
            .extractPageResults(AnalysisType.class);
    assertEquals(actualAllAnalysisTypes.size(), expectedAnalysisTypes.size());
    assertTrue(expectedAnalysisTypes.containsAll(actualAllAnalysisTypes));

    // Some Existing Names
    val someExistingNames = newHashSet(expectedNames);
    randomStream(this::generateUniqueName, 4).forEach(someExistingNames::add);
    val actualSomeAnalysisTypes =
        endpointTester
            .listSchemasGetRequestAnd(
                someExistingNames, null, null, 0, expectedAnalysisTypes.size() * 2, null, null)
            .extractPageResults(AnalysisType.class);
    assertEquals(actualSomeAnalysisTypes.size(), expectedAnalysisTypes.size());
    assertTrue(expectedAnalysisTypes.containsAll(actualSomeAnalysisTypes));

    // No Existing Names
    val noExistingNames = randomStream(this::generateUniqueName, 4).collect(toImmutableSet());
    val actualNoAnalysisTypes =
        endpointTester
            .listSchemasGetRequestAnd(
                noExistingNames, null, null, 0, expectedAnalysisTypes.size() * 2, null, null)
            .extractPageResults(AnalysisType.class);
    assertTrue(actualNoAnalysisTypes.isEmpty());
  }

  private void runInvalidRegisterTest(
      String filename, String expectedMessage, ServerError expectedServerError) {
    val inputInvalidSchema =
        FETCHER.readJsonNode(Paths.get("schema-fixtures/invalid").resolve(filename).toString());
    runInvalidRegisterTest(inputInvalidSchema, expectedMessage, expectedServerError);
  }

  private void runInvalidRegisterTest(
      JsonNode invalidSchema, String expectedMessage, ServerError expectedServerError) {
    val nonExistingName = generateUniqueName();
    val registerRequest =
        RegisterAnalysisTypeRequest.builder().name(nonExistingName).schema(invalidSchema).build();
    val songErrorResponse =
        endpointTester
            .registerAnalysisTypePostRequestAnd(registerRequest)
            .assertIsError()
            .assertServerError(expectedServerError)
            .getResponse();
    val songError = parseErrorResponse(songErrorResponse);
    val actualMessage = songError.getMessage();
    assertEquals(actualMessage, expectedMessage);
  }

  @Test
  public void register_extraFields_schemaViolation() {
    runInvalidRegisterTest(
        "invalid.extra_fields.json",
        "[AnalysisTypeService::schema.violation] - #: extraneous key [$id] is not permitted,#: expected type: Boolean, found: JSONObject",
        SCHEMA_VIOLATION);
  }

  @Test
  public void register_malformedJsonSchema_malformedJsonSchema() {
    runInvalidRegisterTest(
        "invalid.malformed_json_schema.json",
        "[AnalysisTypeService::malformed.json.schema] - #/properties/experiment/properties/something/type: expected type is one of JsonArray or String, found: JsonObject",
        MALFORMED_JSON_SCHEMA);
  }

  @Test
  public void register_missSpeltType_schemaViolation() {
    runInvalidRegisterTest(
        "invalid.miss_spelt_type.json",
        "[AnalysisTypeService::schema.violation] - #: expected type: Boolean, found: JSONObject,#/type: ",
        SCHEMA_VIOLATION);
  }

  @Test
  public void register_missingExperiment_schemaViolation() {
    runInvalidRegisterTest(
        "invalid.missing_experiment.json",
        "[AnalysisTypeService::schema.violation] - #: expected type: Boolean, found: JSONObject,#: extraneous key [$id] is not permitted,#/required: expected at least one array item to match 'contains' schema",
        SCHEMA_VIOLATION);
  }

  @Test
  public void register_misspeltExperiment_schemaViolation() {
    runInvalidRegisterTest(
        "invalid.misspelt_experiment.json",
        "[AnalysisTypeService::schema.violation] - #: expected type: Boolean, found: JSONObject,#: extraneous key [$id] is not permitted,#/properties: required key [experiment] not found",
        SCHEMA_VIOLATION);
  }

  @Test
  public void register_missingType_schemaViolation() {
    runInvalidRegisterTest(
        "invalid.missing_type.json",
        "[AnalysisTypeService::schema.violation] - #: required key [type] not found,#: expected type: Boolean, found: JSONObject",
        SCHEMA_VIOLATION);
  }

  /** Sad Path: test that an error occurs when registering an empty schema */
  @Test
  @Transactional
  public void register_emptySchema_schemaViolation() {
    val r = RegisterAnalysisTypeRequest.builder().name(this.generateUniqueName()).build();
    endpointTester.registerAnalysisTypePostRequestAnd(r).assertServerError(SCHEMA_VIOLATION);

    r.setSchema(mapper().createObjectNode());
    endpointTester.registerAnalysisTypePostRequestAnd(r).assertServerError(SCHEMA_VIOLATION);
  }

  @Test
  public void register_illegalFields_schemaViolation() {
    Stream.of(
            "analysisId",
            "analysisState",
            "studyId",
            "analysisType",
            "analysisTypeId",
            "samples",
            "files")
        .forEach(
            f -> {
              // Create an invalid schema using one of the invalid fields
              val inputInvalidSchema =
                  FETCHER.readJsonNode(Paths.get("schema-fixtures/valid.json").toString());
              val properties = (ObjectNode) inputInvalidSchema.path("properties");
              val field = properties.putObject(f);
              field.put("type", "string");

              log.info("Testing illegal field: " + f);

              // Test
              runInvalidRegisterTest(
                  inputInvalidSchema,
                  "[AnalysisTypeService::schema.violation] - #/properties/"
                      + f
                      + ": subject must not be valid against schema {},#: expected type: Boolean, found: JSONObject",
                  SCHEMA_VIOLATION);
            });
  }

  /** Happy Path: test filtering the listing endpoint by multiple versions only */
  @Test
  @Transactional
  public void listAnalysisTypes_filterByMultipleVersions_success() {
    // Generate data
    val repeats = 10;
    val data = generateData(repeats);
    val totalAnalysisTypes = (int) analysisSchemaRepository.count();

    // Create selected versions to test on
    val selectedVersions = newHashSet(4, 7, 9);
    val selectedAnalysisTypes =
        data.stream()
            .filter(x -> selectedVersions.contains(x.getVersion()))
            .collect(toImmutableSet());
    val selectedAnalysisTypeNames = mapToImmutableSet(selectedAnalysisTypes, AnalysisType::getName);

    // ******************************
    // All existing versions
    // ******************************
    val actualAllAnalysisTypes =
        endpointTester
            .listSchemasGetRequestAnd(
                null, selectedVersions, null, 0, totalAnalysisTypes, null, null)
            .extractPageResults(AnalysisType.class).stream()
            // Since there may be other persisted data with same version number, ignore those
            // analysisTypes
            // since they are outside the scope of this test
            .filter(x -> selectedAnalysisTypeNames.contains(x.getName()))
            .collect(toImmutableList());
    assertEquals(actualAllAnalysisTypes.size(), selectedAnalysisTypes.size());
    assertTrue(selectedAnalysisTypes.containsAll(actualAllAnalysisTypes));

    // ******************************
    // Some existing versions
    // ******************************
    val someExistingSelectedVersions = newHashSet(selectedVersions);
    // These versions were not generated
    someExistingSelectedVersions.add(repeats + 3);
    someExistingSelectedVersions.add(repeats + 7);

    // Ensure that adding additional non-existing version will not affect the end result (its
    // ignored)
    val actualSomeAnalysisTypes =
        endpointTester
            .listSchemasGetRequestAnd(
                null, someExistingSelectedVersions, null, 0, totalAnalysisTypes, null, null)
            .extractPageResults(AnalysisType.class).stream()
            // Since there may be other persisted data with same version number, ignore those
            // analysisTypes
            // since they are outside the scope of this test
            .filter(x -> selectedAnalysisTypeNames.contains(x.getName()))
            .collect(toImmutableList());
    assertEquals(actualSomeAnalysisTypes.size(), selectedAnalysisTypes.size());
    assertTrue(selectedAnalysisTypes.containsAll(actualSomeAnalysisTypes));

    // ******************************
    // No existing versions
    // ******************************
    val noExistingSelectedVersions = newHashSet(repeats + 3, repeats + 7);
    val actualNoAnalysisTypes =
        endpointTester
            .listSchemasGetRequestAnd(
                null, noExistingSelectedVersions, null, 0, totalAnalysisTypes, null, null)
            .extractPageResults(AnalysisType.class).stream()
            // Since there may be other persisted data with same version number, ignore those
            // analysisTypes
            // since they are outside the scope of this test
            .filter(x -> selectedAnalysisTypeNames.contains(x.getName()))
            .collect(toImmutableList());
    assertTrue(actualNoAnalysisTypes.isEmpty());
  }

  private void runLegacyAnalysisTypeTest(String name) {
    val fetcher =
        ResourceFetcher.builder().resourceType(MAIN).dataDir(Paths.get("schemas")).build();

    val results =
        endpointTester
            .listSchemasGetRequestAnd(ImmutableList.of(name), null, null, 0, 100, null, null)
            .extractPageResults(AnalysisType.class);

    val actualNames = mapToImmutableSet(results, AnalysisType::getName);
    assertEquals(actualNames.size(), 1);
    assertTrue(actualNames.contains(name));
  }

  private String generateUniqueName() {
    String analysisType = null;
    do {
      analysisType = randomGenerator.generateRandomAsciiString(10);
    } while (analysisSchemaRepository.countAllByName(analysisType) > 0);
    return analysisType;
  }

  private List<AnalysisType> generateData(int repeats) {
    return generateData(analysisTypeService, repeats);
  }

  /**
   * Generates data given the number of repeats If repeats = N, there will be N analysisType names,
   * and for each analysisType N versions. In total it will generate N*N analysisTypes
   */
  public static List<AnalysisType> generateData(
      AnalysisTypeService analysisTypeService, int repeats) {
    val randomGenerator = createRandomGenerator("temp");
    val names =
        range(0, repeats)
            .boxed()
            .map(x -> "exampleAnalysisType-" + randomGenerator.generateRandomAsciiString(10))
            .collect(toImmutableList());

    return range(0, repeats * repeats)
        .boxed()
        .map(
            i -> {
              val name = names.get(i % repeats);
              val schema = generateRandomRegistrationPayload(randomGenerator);
              return analysisTypeService.register(name, schema);
            })
        .collect(toImmutableList());
  }

  private static final ResourceFetcher FETCHER =
      ResourceFetcher.builder()
          .resourceType(ResourceFetcher.ResourceType.TEST)
          .dataDir(Paths.get("documents/validation/dynamic-analysis-type"))
          .build();

  private static JsonNode generateRandomRegistrationPayload(RandomGenerator r) {
    val json = (ObjectNode) FETCHER.readJsonNode("schema-fixtures/valid.json");
    val propertiesNode = (ObjectNode) json.path("properties");
    val experimentNode = (ObjectNode) propertiesNode.path("experiment");
    experimentNode.put("title", r.generateRandomUUIDAsString());
    //    propertiesNode.put("$comment", r.generateRandomUUIDAsString());
    //    val idNode = propertiesNode.putObject("id");
    //    idNode.put("type", "string");
    //    idNode.put("const", r.generateRandomUUIDAsString());
    return json;
  }
}
