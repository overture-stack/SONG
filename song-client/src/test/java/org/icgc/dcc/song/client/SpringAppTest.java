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


  @Test
  @SneakyThrows
  @Ignore
  public void testUpload(){
    val jsonPath = Paths.get("../sequencingRead.json");
    assertThat(jsonPath).exists();
    executeMain("upload", "-f", jsonPath.toString());
    log.info("sdf");
  }

  @Test
  @SneakyThrows
  @Ignore
  public void testStatus(){
    executeMain("status", "-u", "UP-826f10c8-7933-4160-9530-d24de40890ba");
    log.info("sdf");
  }

  @Test
  @SneakyThrows
  @Ignore
  public void testSave(){
    executeMain("save", "-u", "UP-826f10c8-7933-4160-9530-d24de40890ba");
    log.info("save");
  }

  @Test
  @SneakyThrows
  @Ignore
  public void testManifest(){
    executeMain("manifest", "-a", "86fa41c5-f589-4e8c-bb07-8aaf555e2c6c", "-f", "manifest.txt");

  }

  @Test
  @SneakyThrows
  @Ignore
  public void testSuppress(){
    executeMain("suppress", "-a", "86fa41c5-f589-4e8c-bb07-8aaf555e2c6c");
    log.info("sdf");
  }

}
