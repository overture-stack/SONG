package bio.overture.song.server.controller;

import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.model.dto.AnalysisType;
import bio.overture.song.server.model.dto.schema.RegisterAnalysisTypeRequest;
import bio.overture.song.server.repository.AnalysisSchemaRepository;
import bio.overture.song.server.service.AnalysisTypeService;
import bio.overture.song.server.utils.EndpointTester;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.assertj.core.api.Assertions.assertThat;
import static bio.overture.song.core.utils.JsonUtils.mapper;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.service.AnalysisTypeService.buildAnalysisType;
import static bio.overture.song.server.service.AnalysisTypeService.resolveAnalysisTypeId;
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
  public void register_nonExistingName_success(){
    // Generate unique name and schema, and create request
    val nonExistingName = generateUniqueName();
    val schema = JsonUtils.mapper().createObjectNode()
        .put("$id", randomGenerator.generateRandomUUIDAsString());
    val request = RegisterAnalysisTypeRequest.builder()
        .name(nonExistingName)
        .schema(schema)
        .build();

    // Build the expected AnalysisType using the AnalysisTypeService and also verify proper format
    val expectedAnalysisType = buildAnalysisType(nonExistingName, 1, schema);
    assertThat(resolveAnalysisTypeId(nonExistingName, 1)).isEqualTo(nonExistingName+":1");
    assertThat(expectedAnalysisType.getId()).isEqualTo(resolveAnalysisTypeId(nonExistingName, 1));

    // Assert the schema and name were properly registered
    endpointTester.registerAnalysisTypePostRequestAnd(request)
        .assertEntityOfType(AnalysisType.class)
        .isEqualTo(expectedAnalysisType);
  }

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

    @Test
    public void saveStudyShouldValidateStudyId() {
      generateData2(analysisTypeService, 10);
      val result = endpointTester.getSchemaGetRequestAnd(null, null, null, null, null, null).extractPageResults(AnalysisType.class);
      log.info("sdf");
    }


  public List<AnalysisType> generateData(int repeats) {
    return generateData2(analysisTypeService, repeats);
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

  private String generateUniqueName(){
    String analysisType = null;
    do{
      analysisType = randomGenerator.generateRandomAsciiString(10);
    } while (analysisSchemaRepository.countAllByName(analysisType) > 0);
    return analysisType;
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
