package org.icgc.dcc.sodalite.server.service;

import lombok.SneakyThrows;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class})
@ActiveProfiles("secure")
public class ExistenceServiceTest {
  private static final String BASE_URL = "https://storage.cancercollaboratory.org";
  private static final String OBJ_ID = "cecb35d8-2b3b-5cf6-a775-24eada1c4651";

  @Test
  @SneakyThrows
  public void testGet(){
    val exi = ExistenceService.createExistenceService(BASE_URL);
    val accessToken = "6634c87d-85bb-4ff7-9259-cf57bddf87b0-";
    Assertions.assertThat(exi.isObjectExist(accessToken,OBJ_ID)).isTrue();
  }

}
