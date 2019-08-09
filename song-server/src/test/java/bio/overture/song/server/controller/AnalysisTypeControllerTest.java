package bio.overture.song.server.controller;

import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.model.dto.AnalysisType;
import bio.overture.song.server.model.dto.schema.RegisterAnalysisTypeRequest;
import bio.overture.song.server.repository.AnalysisSchemaRepository;
import bio.overture.song.server.service.AnalysisTypeService;
import bio.overture.song.server.utils.EndpointTester;
import bio.overture.song.server.utils.ResourceFetcher;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.everit.json.schema.Schema;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Supplier;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static net.javacrumbs.jsonunit.JsonAssert.when;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_TYPE_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerErrors.MALFORMED_PARAMETER;
import static bio.overture.song.core.exceptions.ServerErrors.SCHEMA_VIOLATION;
import static bio.overture.song.core.utils.JsonUtils.mapper;
import static bio.overture.song.core.utils.JsonUtils.readTree;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.core.utils.RandomGenerator.randomList;
import static bio.overture.song.core.utils.RandomGenerator.randomStream;
import static bio.overture.song.server.controller.analysisType.AnalysisTypePageableResolver.DEFAULT_LIMIT;
import static bio.overture.song.server.service.AnalysisTypeService.buildAnalysisType;
import static bio.overture.song.server.service.AnalysisTypeService.resolveAnalysisTypeId;
import static bio.overture.song.server.utils.CollectionUtils.mapToImmutableSet;
import static bio.overture.song.server.utils.EndpointTester.createEndpointTester;
import static bio.overture.song.server.utils.ResourceFetcher.ResourceType.MAIN;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc(secure = false)
@ActiveProfiles({ "test" })
public class AnalysisTypeControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private Supplier<Schema> analysisTypeMetaSchemaSupplier;

  @Autowired
  private AnalysisTypeService analysisTypeService;

  @Autowired
  private AnalysisSchemaRepository analysisSchemaRepository;

  private EndpointTester endpointTester;
  private RandomGenerator randomGenerator;

  @Before
  public void beforeTest(){
    this.endpointTester = createEndpointTester(mockMvc, true);
    this.randomGenerator = createRandomGenerator(getClass().getCanonicalName());
  }

  /**
   * Unhappy Path: test a schema violation server error is returned when trying to
   * register an invalid schema with a valid name
   */
  @Test
  public void register_invalidSchema_schemaViolation(){
    // Generate test data
    val nonExistingName = generateUniqueName();

    // Generate valid schema
    val invalidSchema = mapper().createObjectNode();
    invalidSchema.putObject("$id").put("someKey", "someValue");

    // Assert registration of malformedNames results in MALFORMED_PARAMETER error
    val request = RegisterAnalysisTypeRequest.builder()
        .name(nonExistingName)
        .schema(invalidSchema)
        .build();
    endpointTester.registerAnalysisTypePostRequestAnd(request)
        .assertServerError(SCHEMA_VIOLATION);
  }

  /**
   * Unhappy Path: test a malformed parameter error is returned when trying to
   * register a valid schema with a malformed name
   */
  @Test
  public void register_malformedName_malformedParameter(){
    // Generate test data
    val p = randomGenerator.generateRandomAsciiString(10);
    val s = randomGenerator.generateRandomAsciiString(10);
    val malformedNames = newHashSet(
        p+"$"+s,
        p+">"+s,
        p+" "+s,
        p+","+s,
        p+":"+s,
        " "+p+s,
        p+s+" ",
        " "+p+s+" "
    );

    // Generate valid schema
    val schema = mapper().createObjectNode()
        .put("$id", randomGenerator.generateRandomUUIDAsString());

    // Assert registration of malformedNames results in MALFORMED_PARAMETER error
    malformedNames.forEach(name -> {
      val request = RegisterAnalysisTypeRequest.builder()
          .name(name)
          .schema(schema)
          .build();
      endpointTester.registerAnalysisTypePostRequestAnd(request)
          .assertServerError(MALFORMED_PARAMETER);
    });
  }

  /**
   * Happy Path: Test successful initial registration of an analysisType as well as an update, resulting in 2 versions
   */
  @Test
  @Transactional
  public void createAndUpdate_nonExistingName_success(){
    // Generate unique name and schema, and create request
    val nonExistingName = generateUniqueName();
    val createSchema = mapper().createObjectNode()
        .put("$id", randomGenerator.generateRandomUUIDAsString());
    val createRequest = RegisterAnalysisTypeRequest.builder()
        .name(nonExistingName)
        .schema(createSchema)
        .build();

    // Build the expected AnalysisType using the AnalysisTypeService and also verify proper format
    val expectedAnalysisType = buildAnalysisType(nonExistingName, 1, createSchema);
    assertThat(resolveAnalysisTypeId(nonExistingName, 1)).isEqualTo(nonExistingName+":1");
    assertThat(expectedAnalysisType.getId()).isEqualTo(resolveAnalysisTypeId(nonExistingName, 1));

    // Assert the schema and name were properly registered
    endpointTester.registerAnalysisTypePostRequestAnd(createRequest)
        .assertEntityOfType(AnalysisType.class)
        .isEqualTo(expectedAnalysisType);

    // Update the schema for the same analysisTypeName
    val updateSchema = mapper().createObjectNode()
        .put("$id", randomGenerator.generateRandomUUIDAsString());
    val updateRequest = RegisterAnalysisTypeRequest.builder()
        .name(nonExistingName)
        .schema(updateSchema)
        .build();
    val expectedAnalysisTypeUpdate = buildAnalysisType(nonExistingName, 2, updateSchema);

    // Assert the schema and name were properly registered
    endpointTester.registerAnalysisTypePostRequestAnd(updateRequest)
        .assertEntityOfType(AnalysisType.class)
        .isEqualTo(expectedAnalysisTypeUpdate);

    // Assert there are only 2 entries for the analysisType name
    val results = endpointTester.getSchemaGetRequestAnd(ImmutableList.of(nonExistingName),
        null, 0, 100, null, null)
        .extractPageResults(AnalysisType.class);
    val actualNames = mapToImmutableSet(results, AnalysisType::getName);
    assertThat(actualNames).hasSize(1);
    assertThat(actualNames).contains(nonExistingName);
  }

  /**
   * Happy Path: Test an analysisType can be read by requesting it by an analysisTypeId in the form <name>:<version>
   */
  @Test
  @Transactional
  public void getAnalysisTypeByVersion_existing_success(){
    // Generate data
    val data = generateData(10);
    val expectedAnalysisType = randomGenerator.randomElement(data);

    // Get the analysisTypeId using the service and assert it has the correct format
    val requestAnalysisTypeId = resolveAnalysisTypeId(expectedAnalysisType);
    assertThat(requestAnalysisTypeId).isEqualTo(expectedAnalysisType.getName()+":"+expectedAnalysisType.getVersion());

    // Assert the actual retrieved resource matches the expected
    endpointTester.getAnalysisTypeVersionGetRequestAnd(requestAnalysisTypeId)
        .assertEntityOfType(AnalysisType.class)
        .isEqualTo(expectedAnalysisType);
  }

  /**
   * Test that the meta schema can be requested
   */
  @Test
  @SneakyThrows
  public void getMetaSchema_existing_success(){
    // Expected meta schema json
    val expected = readTree(analysisTypeMetaSchemaSupplier.get().toString());

    // Assert the actual retrieved resource matches the expected
    val actual = endpointTester.getMetaSchemaGetRequestAnd()
        .extractOneEntity(JsonNode.class);
    assertJsonEquals(expected, actual, when(IGNORING_ARRAY_ORDER));
  }

  @Test
  @Ignore
  @SneakyThrows
  public void getLegacyVariantCall_existing_success(){
    runLegacyVariantCallTest("variantCall");
  }

  @Test
  @Ignore
  @SneakyThrows
  public void getLegacySequencingRead_existing_success(){
    runLegacyVariantCallTest("sequencingRead");
  }

  /**
   * Test the default size is DEFAULT_LIMIT
   */
  @Test
  public void listAnalysisTypesDefaultSize_exist_success() {
    // Generate data
    generateData(10);
    val actualAnalysisTypes = endpointTester
        .getSchemaGetRequestAnd(null, null, null, null, null, null)
        .extractPageResults(AnalysisType.class);

    // Assert default size is DEFAULT_LIMIT
    assertThat(DEFAULT_LIMIT).isEqualTo(20);
    assertThat(actualAnalysisTypes).hasSize(DEFAULT_LIMIT);
  }

  /**
   * Happy Path: test filtering the listing endpoint by multiple versions only
   */
  @Test
  @Transactional
  public void listFilterMultipleVersions_mulitipleVersions_success() {
    // Generate data
    val repeats = 10;
    val data = generateData(repeats);
    val totalAnalysisTypes = (int)analysisSchemaRepository.count();

    // Create selected versions to test on
    val selectedVersions = newHashSet(4,7,9);
    val selectedAnalysisTypes = data.stream()
        .filter(x -> selectedVersions.contains(x.getVersion()))
        .collect(toImmutableSet());
    val selectedAnalysisTypeNames = mapToImmutableSet(selectedAnalysisTypes, AnalysisType::getName);

    // ******************************
    // All existing versions
    // ******************************
    val actualAllAnalysisTypes = endpointTester
        .getSchemaGetRequestAnd(null,
            selectedVersions, 0, totalAnalysisTypes, null, null)
        .extractPageResults(AnalysisType.class)
        .stream()
        // Since there may be other persisted data with same version number, ignore those analysisTypes
        // since they are outside the scope of this test
        .filter(x -> selectedAnalysisTypeNames.contains(x.getName()))
        .collect(toImmutableList());
    assertThat(actualAllAnalysisTypes).hasSameSizeAs(selectedAnalysisTypes);
    assertThat(selectedAnalysisTypes).containsExactlyInAnyOrderElementsOf(actualAllAnalysisTypes);

    // ******************************
    // Some existing versions
    // ******************************
    val someExistingSelectedVersions = newHashSet(selectedVersions);
    // These versions were not generated
    someExistingSelectedVersions.add(repeats+3);
    someExistingSelectedVersions.add(repeats+7);

    // Ensure that adding additional non-existing version will not affect the end result (its ignored)
    val actualSomeAnalysisTypes = endpointTester
        .getSchemaGetRequestAnd(null,
            someExistingSelectedVersions, 0, totalAnalysisTypes, null, null)
        .extractPageResults(AnalysisType.class)
        .stream()
        // Since there may be other persisted data with same version number, ignore those analysisTypes
        // since they are outside the scope of this test
        .filter(x -> selectedAnalysisTypeNames.contains(x.getName()))
        .collect(toImmutableList());
    assertThat(actualSomeAnalysisTypes).hasSameSizeAs(selectedAnalysisTypes);
    assertThat(selectedAnalysisTypes).containsExactlyInAnyOrderElementsOf(actualSomeAnalysisTypes);

    // ******************************
    // No existing versions
    // ******************************
    val noExistingSelectedVersions = newHashSet(repeats+3, repeats+7);
    val actualNoAnalysisTypes = endpointTester
        .getSchemaGetRequestAnd(null,
            noExistingSelectedVersions, 0, totalAnalysisTypes, null, null)
        .extractPageResults(AnalysisType.class)
        .stream()
        // Since there may be other persisted data with same version number, ignore those analysisTypes
        // since they are outside the scope of this test
        .filter(x -> selectedAnalysisTypeNames.contains(x.getName()))
        .collect(toImmutableList());
    assertThat(actualNoAnalysisTypes).isEmpty();
  }

  /**
   *  Unhappy Path: test that a NOT_FOUND status code is returned when retrieving a non-existent analysisType name
   */
  @Test
  public void getAnalysisTypeByVersion_nonExistingName_notFound(){
    val nonExistingName = generateUniqueName();
    val analysisTypeId = resolveAnalysisTypeId(nonExistingName, 1);
    endpointTester.getAnalysisTypeVersionGetRequestAnd(analysisTypeId).assertServerError(ANALYSIS_TYPE_NOT_FOUND);
  }

  /**
   *  Unhappy Path: test that a NOT_FOUND status code is returned when retrieving a non-existent analysisType version
   *  for an existing name
   */
  @Test
  @Transactional
  public void getAnalysisTypeByVersion_nonExistingVersion_notFound(){
    val existingAnalysisType = generateData(1).get(0);
    val nonExistingVersion = existingAnalysisType.getVersion()+1;
    val analysisTypeId = resolveAnalysisTypeId(existingAnalysisType.getName(), nonExistingVersion);
    endpointTester.getAnalysisTypeVersionGetRequestAnd(analysisTypeId).assertServerError(ANALYSIS_TYPE_NOT_FOUND);
  }

  /**
   * Unhappy Path: test that malformed analysisTypeIds return a malformedParameter
   */
  @Test
  public void getAnalysisTypeByVersion_malformedId_malformedParameter(){
    val malformedIds = newHashSet("som3th!ng$:4", "something-4", "something:bad", "something:-7", "something:1.0", "something4" );
    malformedIds.forEach( analysisTypeId ->
        endpointTester.getAnalysisTypeVersionGetRequestAnd(analysisTypeId).assertServerError(MALFORMED_PARAMETER)
    );
  }

  @Test
  @Transactional
  public void listFilter_multiNamesVersions_success(){
    // Generate data
    val repeats = 10;
    val numSelectedNames = 3;
    val numSelectedVersions = 4;
    val data = generateData(repeats);
    val selectedNames = ImmutableSet.copyOf(randomGenerator.randomSublist(newArrayList(mapToImmutableSet(data, AnalysisType::getName)), numSelectedNames));
    val selectedVersions = ImmutableSet.copyOf(randomGenerator.randomSublist(range(1, repeats+1).boxed().collect(toImmutableList()), numSelectedVersions));
    val selectedData = data.stream()
        .filter(x -> selectedNames.contains(x.getName()) && selectedVersions.contains(x.getVersion()))
        .collect(toImmutableSet());

    // ********************************
    // All selected names and versions
    // ********************************
    val actualAllAnalysisTypes = endpointTester
        .getSchemaGetRequestAnd(selectedNames,
            selectedVersions, 0, selectedData.size()*2, null, null)
        .extractPageResults(AnalysisType.class);
    assertThat(actualAllAnalysisTypes).hasSameSizeAs(selectedData);
    assertThat(selectedData).containsExactlyInAnyOrderElementsOf(actualAllAnalysisTypes);

    // ********************************
    // Some selected names and versions
    // ********************************
    val someSelectedNames = newHashSet(selectedNames);
    someSelectedNames.add(generateUniqueName());
    someSelectedNames.add(generateUniqueName());

    val someSelectedVersions = newHashSet(selectedVersions);
    someSelectedVersions.add(repeats+3);
    someSelectedVersions.add(repeats+7);

    val actualSomeAnalysisTypes = endpointTester
        .getSchemaGetRequestAnd(someSelectedNames,
            someSelectedVersions, 0, selectedData.size()*2, null, null)
        .extractPageResults(AnalysisType.class);
    assertThat(actualSomeAnalysisTypes).hasSameSizeAs(selectedData);
    assertThat(selectedData).containsExactlyInAnyOrderElementsOf(actualSomeAnalysisTypes);

    // ********************************
    // No selected names and versions
    // ********************************
    val noSelectedNames = newHashSet(randomList(this::generateUniqueName, 10));
    val noSelectedVersions = newHashSet(repeats+3, repeats+7);

    val actualNoAnalysisTypes = endpointTester
        .getSchemaGetRequestAnd(noSelectedNames,
            noSelectedVersions, 0, selectedData.size()*2, null, null)
        .extractPageResults(AnalysisType.class);
    assertThat(actualNoAnalysisTypes).isEmpty();

    // ********************************
    // Existing name, non-existing version
    // ********************************
    val actualNoAnalysisTypes2 = endpointTester
        .getSchemaGetRequestAnd(selectedNames,
            noSelectedVersions, 0, selectedData.size()*2, null, null)
        .extractPageResults(AnalysisType.class);
    assertThat(actualNoAnalysisTypes2).isEmpty();

    // ********************************
    // Non-existing name, existing versions
    // ********************************
    val actualNoAnalysisTypes3 = endpointTester
        .getSchemaGetRequestAnd(noSelectedNames,
            selectedVersions, 0, selectedData.size()*2, null, null)
        .extractPageResults(AnalysisType.class);
    assertThat(actualNoAnalysisTypes3).isEmpty();
  }

  /**
   * Unhappy Path: test that malformed analysisTypeIds return a malformedParameter
   */
  @Test
  public void getAnalysisTypeByVersion_malformedId_malformedParameter(){
    val malformedIds = newHashSet("som3th!ng$:4", "something-4", "something:bad", "something:-7", "something:1.0", "something4" );
    malformedIds.forEach( analysisTypeId ->
        endpointTester.getAnalysisTypeVersionGetRequestAnd(analysisTypeId).assertStatusCode(MALFORMED_PARAMETER)
    );
  }

  /**
   * Happy Path: test filtering the listing endpoint by multiple names only
   */
  @Test
  @Transactional
  public void listFilterMultipleNames_multipleNames_success(){
    // Generate data
    val repeats = 10;
    val data = generateData(repeats);
    val names = ImmutableList.copyOf(mapToImmutableSet(data, AnalysisType::getName));

    val expectedNames = ImmutableSet.copyOf(randomGenerator.randomSublist(names, names.size()/2));
    val expectedAnalysisTypes = data.stream()
        .filter(x -> expectedNames.contains(x.getName()))
        .collect(toList());


    // All Existing Names
    val actualAllAnalysisTypes = endpointTester
        .getSchemaGetRequestAnd(expectedNames,
            null, 0, expectedAnalysisTypes.size()*2, null, null)
        .extractPageResults(AnalysisType.class);
    assertThat(actualAllAnalysisTypes).hasSameSizeAs(expectedAnalysisTypes);
    assertThat(expectedAnalysisTypes).containsExactlyInAnyOrderElementsOf(actualAllAnalysisTypes);

    // Some Existing Names
    val someExistingNames = newHashSet(expectedNames);
    randomStream(this::generateUniqueName, 4).forEach(someExistingNames::add);
    val actualSomeAnalysisTypes = endpointTester
        .getSchemaGetRequestAnd(someExistingNames,
            null, 0, expectedAnalysisTypes.size()*2, null, null)
        .extractPageResults(AnalysisType.class);
    assertThat(actualSomeAnalysisTypes).hasSameSizeAs(expectedAnalysisTypes);
    assertThat(expectedAnalysisTypes).containsExactlyInAnyOrderElementsOf(actualSomeAnalysisTypes);

    // No Existing Names
    val noExistingNames = randomStream(this::generateUniqueName, 4).collect(toImmutableSet());
    val actualNoAnalysisTypes = endpointTester
        .getSchemaGetRequestAnd(noExistingNames,
            null, 0, expectedAnalysisTypes.size()*2, null, null)
        .extractPageResults(AnalysisType.class);
    assertThat(actualNoAnalysisTypes).isEmpty();
  }

  private void runLegacyVariantCallTest(String name) {
    val fetcher =
        ResourceFetcher.builder().resourceType(MAIN).dataDir(Paths.get("schemas")).build();
    val expected = fetcher.readJsonNode(name + ".json");

    val results =
        endpointTester
            .getSchemaGetRequestAnd(ImmutableList.of(name), null, 0, 100, null, null)
            .extractPageResults(AnalysisType.class);

    val actualNames = mapToImmutableSet(results, AnalysisType::getName);
    assertThat(actualNames).hasSize(1);
    assertThat(actualNames).contains(name);
  }

  private List<AnalysisType> generateData(int repeats) {
    return generateData2(analysisTypeService, repeats);
  }

  private String generateUniqueName(){
    String analysisType = null;
    do{
      analysisType = randomGenerator.generateRandomAsciiString(10);
    } while (analysisSchemaRepository.countAllByName(analysisType) > 0);
    return analysisType;
  }

  /**
   * Generates data given the number of repeats
   * If repeats = N, there will be N analysisType names, and for each analysisType N versions. In total it will generate N*N analysisTypes
   */
  public static List<AnalysisType> generateData2(AnalysisTypeService analysisTypeService, int repeats) {
    val randomGenerator = createRandomGenerator("temp");
    val names = range(0, repeats)
        .boxed()
        .map(x -> "exampleAnalysisType-" + randomGenerator.generateRandomAsciiString(10))
        .collect(toImmutableList());

    return range(0, repeats * repeats)
        .boxed()
        .map(i -> {
          val name = names.get(i % repeats);
          val schema = mapper().createObjectNode().put("$id", randomGenerator.generateRandomUUIDAsString());
          return analysisTypeService.register(name, schema);
        })
        .collect(toImmutableList());
  }



    //GET SCHEMA
    // 2 events:   filter and view
    //  filter : name or version
    //  view:  limit, offset, sort, sortOrder
    // make method that mixes the 2 conifigurations
  // shoudl have 2^2 * 4^2 = 64 tests


    //POST SCHEMA
    //


}
