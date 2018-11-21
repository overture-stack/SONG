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

import static bio.overture.song.core.exceptions.ServerErrors.STUDY_ID_MISMATCH;
import static bio.overture.song.server.utils.EndpointTester.createEndpointTester;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc(secure = false)
@ActiveProfiles({ "dev", "test" })
public class StudyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private EndpointTester endpointTester;

    @Before
    public void beforeTest(){
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
