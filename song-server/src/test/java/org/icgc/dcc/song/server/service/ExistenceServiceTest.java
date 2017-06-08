package org.icgc.dcc.song.server.service;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.System.getProperty;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class})
@ActiveProfiles("secure,test,dev")
public class ExistenceServiceTest {


  @Value("${dcc-storage.url}")
  private String storageUrl;

  @Test
  @SneakyThrows
  @Ignore
  public void testGet(){
    val exi = ExistenceService.createExistenceService(storageUrl);
    val testObjId = "cecb35d8-2b3b-5cf6-a775-24eada1c4651";
    val accessToken = getProperty("token");
    checkNotNull(accessToken, "Must define token for quick test");
    assertThat(exi.isObjectExist(accessToken,testObjId)).isTrue();
  }


}
