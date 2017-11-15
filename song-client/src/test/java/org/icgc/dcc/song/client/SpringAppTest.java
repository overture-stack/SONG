package org.icgc.dcc.song.client;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class SpringAppTest extends AbstractClientMainTest{

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  /**
   * Placeholder test for debuging the client
   */
  @Test
  @SneakyThrows
  @Ignore
  public void testUpload(){
    val jsonPath = Paths.get("../src/test/resources/fixtures/sequencingRead.json");
    assertThat(jsonPath).exists();
    executeMain("upload", "-f", jsonPath.toString());
    log.info("sdf");
  }

}
