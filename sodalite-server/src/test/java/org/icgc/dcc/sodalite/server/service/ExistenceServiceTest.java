package org.icgc.dcc.sodalite.server.service;

import lombok.SneakyThrows;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;

import static java.lang.System.getProperty;
import static java.util.Objects.requireNonNull;

public class ExistenceServiceTest {
  private static final String BASE_URL = "https://storage.cancercollaboratory.org";
  private static final String OBJ_ID = "cecb35d8-2b3b-5cf6-a775-24eada1c4651";

  @Test
  @SneakyThrows
  @Ignore
  public void testGet(){
    val exi = ExistenceService.createExistenceService(BASE_URL);
    val accessToken = requireNonNull(getProperty("token"));
    Assertions.assertThat(exi.isObjectExist(accessToken,OBJ_ID)).isTrue();
  }

}
