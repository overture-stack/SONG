package bio.overture.song.server.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static bio.overture.song.core.exceptions.ServerErrors.STUDY_ID_MISMATCH;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc(secure = false)
@ActiveProfiles({ "dev", "test" })
public class StudyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void saveStudyShouldValidateStudyId() throws Exception {
        this.mockMvc
            .perform(
                post("/studies/123/")
                    .contentType(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .content("{\"studyId\": \"456\"}"))
            .andExpect(status().is(STUDY_ID_MISMATCH.getHttpStatus().value()));
    }

}
