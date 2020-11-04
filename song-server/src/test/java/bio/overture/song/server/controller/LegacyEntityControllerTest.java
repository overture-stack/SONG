package bio.overture.song.server.controller;

import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.server.model.legacy.LegacyDto;
import bio.overture.song.server.model.legacy.LegacyEntity;
import bio.overture.song.server.service.LegacyEntityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@RunWith(SpringRunner.class)
@WebMvcTest(LegacyEntityController.class)
@AutoConfigureMockMvc(secure = false)
@ActiveProfiles({"test"})
public class LegacyEntityControllerTest {
    private final ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private LegacyEntityService service;

    private static final LegacyDto someValidLegacyDto = LegacyDto.builder().gnosId("gnosId1").build();

    private static final LegacyEntity entity0 = new LegacyEntity("id1", "gnosId1", "file1", "project1", "controlled");
    private static final LegacyEntity entity1 = new LegacyEntity("id2", "gnosId1", "file2", "project1", "controlled");
    private static final LegacyEntity entity2 = new LegacyEntity("id3", "gnosId1", "file3", "project1", "controlled");
    private static final LegacyEntity entity3 = new LegacyEntity("id4", "gnosId1", "file4", "project1", "controlled");

    public LegacyEntityControllerTest() {
        mapper = JsonUtils.mapper();
    }

    @Test
    @SneakyThrows
    public void testPageablePagingWorks() {
        val givenPage0 = new PageRequest(0, 2, Sort.Direction.ASC, "id");
        val givenPage1 = new PageRequest(1, 2, Sort.Direction.ASC, "id");

        val page0 = createPageOf(entity0, entity1);
        val page1 = createPageOf(entity2, entity3);

        given(service.find(any(), eq(someValidLegacyDto), eq(givenPage0))).willReturn(mapper.valueToTree(page0));
        given(service.find(any(), eq(someValidLegacyDto), eq(givenPage1))).willReturn(mapper.valueToTree(page1));

        mvc.perform(get("/entities?gnosId=gnosId1&size=2&page=0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0]").value(entity0))
                .andExpect(jsonPath("$.content[1]").value(entity1))
                .andExpect(jsonPath("$.content[2]").doesNotExist());


        mvc.perform(get("/entities?gnosId=gnosId1&size=2&page=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0]").value(entity2))
                .andExpect(jsonPath("$.content[1]").value(entity3))
                .andExpect(jsonPath("$.content[2]").doesNotExist());
    }

    @Test
    @SneakyThrows
    public void testPageableSizingWorks() {
        val page0Size1Request = new PageRequest(0, 1, Sort.Direction.ASC, "id");
        val page0Size2Request = new PageRequest(0, 2, Sort.Direction.ASC, "id");

        val page0Size1 = createPageOf(entity0);
        val page0Size2 = createPageOf(entity0, entity1);

        given(service.find(any(), eq(someValidLegacyDto), eq(page0Size1Request))).willReturn(mapper.valueToTree(page0Size1));
        given(service.find(any(), eq(someValidLegacyDto), eq(page0Size2Request))).willReturn(mapper.valueToTree(page0Size2));

        mvc.perform(get("/entities?gnosId=gnosId1&size=1&page=0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0]").value(entity0))
                .andExpect(jsonPath("$.content[1]").doesNotExist());


        mvc.perform(get("/entities?gnosId=gnosId1&size=2&page=0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0]").value(entity0))
                .andExpect(jsonPath("$.content[1]").value(entity1))
                .andExpect(jsonPath("$.content[2]").doesNotExist());;
    }

    private Page<LegacyEntity> createPageOf(LegacyEntity... entities) {
        return  new PageImpl<>(List.of(entities));
    }
}
