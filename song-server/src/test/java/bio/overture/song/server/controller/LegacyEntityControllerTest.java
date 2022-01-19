package bio.overture.song.server.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.server.model.legacy.LegacyDto;
import bio.overture.song.server.model.legacy.LegacyEntity;
import bio.overture.song.server.service.LegacyEntityService;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@Slf4j
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@ActiveProfiles({"test", "secure"})
@SpringBootTest
public class LegacyEntityControllerTest {
  @Autowired private MockMvc mvc;

  @MockBean private LegacyEntityService service;

  private static final List<LegacyEntity> entities =
      List.of(
          new LegacyEntity("id1", "gnosId1", "file1", "project1", "controlled"),
          new LegacyEntity("id2", "gnosId1", "file2", "project1", "controlled"),
          new LegacyEntity("id3", "gnosId1", "file3", "project1", "controlled"),
          new LegacyEntity("id4", "gnosId1", "file4", "project1", "controlled"));

  @Before
  public void setup() {
    val gnosId1LegacyDto = LegacyDto.builder().gnosId("gnosId1").build();

    val page0Size1Request = PageRequest.of(0, 1, Sort.Direction.ASC, "id");
    val page0Size2Request = PageRequest.of(0, 2, Sort.Direction.ASC, "id");
    val page1Size2Request = PageRequest.of(1, 2, Sort.Direction.ASC, "id");

    val page0Size1 = createPageOf(entities.get(0));
    val page0Size2 = createPageOf(entities.get(0), entities.get(1));
    val page1Size2 = createPageOf(entities.get(2), entities.get(3));

    val mapper = JsonUtils.mapper();

    given(service.find(any(), eq(gnosId1LegacyDto), eq(page0Size2Request)))
        .willReturn(mapper.valueToTree(page0Size2));
    given(service.find(any(), eq(gnosId1LegacyDto), eq(page1Size2Request)))
        .willReturn(mapper.valueToTree(page1Size2));
    given(service.find(any(), eq(gnosId1LegacyDto), eq(page0Size1Request)))
        .willReturn(mapper.valueToTree(page0Size1));
  }

  @Test
  @SneakyThrows
  public void testPageablePagingWorks() {
    mvc.perform(get("/entities?gnosId=gnosId1&size=2&page=0"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0]").value(entities.get(0)))
        .andExpect(jsonPath("$.content[1]").value(entities.get(1)))
        .andExpect(jsonPath("$.content[2]").doesNotExist());

    mvc.perform(get("/entities?gnosId=gnosId1&size=2&page=1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0]").value(entities.get(2)))
        .andExpect(jsonPath("$.content[1]").value(entities.get(3)))
        .andExpect(jsonPath("$.content[2]").doesNotExist());
  }

  @Test
  @SneakyThrows
  public void testPageableSizingWorks() {
    mvc.perform(get("/entities?gnosId=gnosId1&size=1&page=0"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0]").value(entities.get(0)))
        .andExpect(jsonPath("$.content[1]").doesNotExist());

    mvc.perform(get("/entities?gnosId=gnosId1&size=2&page=0"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0]").value(entities.get(0)))
        .andExpect(jsonPath("$.content[1]").value(entities.get(1)))
        .andExpect(jsonPath("$.content[2]").doesNotExist());
  }

  private Page<LegacyEntity> createPageOf(LegacyEntity... entities) {
    return new PageImpl<>(List.of(entities));
  }
}
