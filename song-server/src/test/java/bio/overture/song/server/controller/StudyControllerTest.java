package bio.overture.song.server.controller;

import bio.overture.song.server.utils.EndpointTester;
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

import static bio.overture.song.core.exceptions.ServerErrors.STUDY_ID_MISMATCH;
import static bio.overture.song.server.utils.EndpointTester.createEndpointTester;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc(secure = false)
@ActiveProfiles({ "test" })
public class StudyControllerTest {

    //This was done because the autowired mockMvc wasn't working properly, it was getting http 403 errors
    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;

    private EndpointTester endpointTester;

    @Before
    public void beforeTest(){
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        this.endpointTester = createEndpointTester(mockMvc);
    }

    @Test
    public void saveStudyShouldValidateStudyId() {
        endpointTester.testPostError(
            "/studies/123/",
            "{\"studyId\": \"456\"}",
            STUDY_ID_MISMATCH
        );
    }

}
