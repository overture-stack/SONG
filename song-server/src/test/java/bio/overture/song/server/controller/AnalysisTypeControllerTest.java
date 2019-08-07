package bio.overture.song.server.controller;

import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.model.dto.AnalysisType;
import bio.overture.song.server.model.dto.schema.RegisterAnalysisTypeRequest;
import bio.overture.song.server.repository.AnalysisSchemaRepository;
import bio.overture.song.server.service.AnalysisTypeService;
import bio.overture.song.server.utils.EndpointTester;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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

import javax.transaction.Transactional;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static net.javacrumbs.jsonunit.JsonAssert.when;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;
import static bio.overture.song.core.utils.JsonUtils.mapper;
import static bio.overture.song.core.utils.JsonUtils.readTree;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.service.AnalysisTypeService.buildAnalysisType;
import static bio.overture.song.server.service.AnalysisTypeService.resolveAnalysisTypeId;
import static bio.overture.song.server.utils.CollectionUtils.mapToImmutableSet;
import static bio.overture.song.server.utils.EndpointTester.createEndpointTester;

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
   * Happy Path: Test successful initial registration of an analysisType as well as an update, resulting in 2 versions
   */
  @Test
  @Transactional
  public void createAndUpdate_nonExistingName_success(){
    // Generate unique name and schema, and create request
    val nonExistingName = generateUniqueName();
    val createSchema = JsonUtils.mapper().createObjectNode()
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
    val updateSchema = JsonUtils.mapper().createObjectNode()
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
  @Transactional
  public void getMetaSchema_existing_success(){
    // Expected meta schema json
    val expected = readTree(analysisTypeMetaSchemaSupplier.get().toString());

    // Assert the actual retrieved resource matches the expected
    val actual = endpointTester.getMetaSchemaGetRequestAnd()
        .extractOneEntity(JsonNode.class);
    assertJsonEquals(expected, actual, when(IGNORING_ARRAY_ORDER));
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

  public static List<AnalysisType> generateData2(AnalysisTypeService analysisTypeService, int repeats) {
    val randomGenerator = createRandomGenerator("temp");
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



    //GET SCHEMA
    // 2 events:   filter and view
    //  filter : name or version
    //  view:  limit, offset, sort, sortOrder
    // make method that mixes the 2 conifigurations
  // shoudl have 2^2 * 4^2 = 64 tests


    //POST SCHEMA
    //


}
