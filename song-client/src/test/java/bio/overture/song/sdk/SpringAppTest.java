/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package bio.overture.song.sdk;

import bio.overture.song.client.cli.ClientMain;
import bio.overture.song.client.config.CustomRestClientConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class SpringAppTest extends AbstractClientMainTest {

  @Rule public TemporaryFolder tmp = new TemporaryFolder();

  /** Placeholder test for debuging the client */
  @Test
  @SneakyThrows
  @Ignore
  public void testUpload() {
    val jsonPath = Paths.get("../src/test/resources/fixtures/sequencingRead.json");
    assertTrue(Files.exists(jsonPath));
    executeMain("upload", "-f", jsonPath.toString());
    log.info("sdf");
  }

  @Test
  @SneakyThrows
  @Ignore
  public void testPing() {
    executeMain("application.yml", "ping");
    log.info("sdf");
  }

  @Mock private SongApi songApi;
  @Mock private CustomRestClientConfig customRestClientConfig;
  @Mock private ManifestClient manifestClient;

  @Override
  protected ClientMain getClientMain() {
    // Needs to be a new instance, to avoid appending status
    return new ClientMain(customRestClientConfig, songApi, manifestClient);
  }

  @Test
  public void test() {
    when(songApi.isAlive()).thenReturn(true);
    executeMain("ping");
    assertTrue(getExitCode() == 0);
    assertTrue(getOutput().contains("true"));

    when(songApi.isAlive()).thenReturn(false);
    executeMain("ping");
    assertTrue(getExitCode() == 0);
    assertTrue(getOutput().contains("false"));
  }
}
