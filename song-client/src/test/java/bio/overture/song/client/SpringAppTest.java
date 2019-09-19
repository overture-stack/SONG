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

package bio.overture.song.client;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

@Slf4j
public class SpringAppTest extends AbstractClientMainTest {

  @Rule public TemporaryFolder tmp = new TemporaryFolder();

  /** Placeholder test for debuging the client */
  @Test
  @SneakyThrows
  @Ignore
  public void testUpload() {
//    val jsonPath = Paths.get("../src/test/resources/fixtures/sequencingRead.json");
    val jsonPath = Paths.get("../song-docker-demo/example/exampleVariantCall.json");
    assertTrue(Files.exists(jsonPath));
    executeMain("upload", "-f", jsonPath.toString());
    log.info("sdf");
  }
}
