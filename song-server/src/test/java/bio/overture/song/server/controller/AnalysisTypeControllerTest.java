package bio.overture.song.server.controller;

import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.model.dto.AnalysisType;
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

import java.util.List;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static bio.overture.song.core.utils.JsonUtils.mapper;
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

    private EndpointTester endpointTester;

    @Before
    public void beforeTest(){
        this.endpointTester = createEndpointTester(mockMvc, true);
    }

    @Test
    public void saveStudyShouldValidateStudyId() {
      generateData2(analysisTypeService, 10);
      val result = endpointTester.getSchemaGetRequestAnd(null, null, null, null, null, null).extractPageResults(AnalysisType.class);
      log.info("sdf");
    }

    public static List<AnalysisType> generateData2(AnalysisTypeService analysisTypeService, int repeats) {
        val randomGenerator = RandomGenerator.createRandomGenerator("temp");
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
