/*
 * Copyright (c) 2019. Ontario Institute for Cancer Research
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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import bio.overture.song.client.cli.ClientMain;
import bio.overture.song.client.config.CustomRestClientConfig;
import bio.overture.song.sdk.ManifestClient;
import bio.overture.song.sdk.SongApi;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class SadPathClientMainTest extends AbstractClientMainTest {

  private static final String DUMMY_STUDY_ID = "ABC123";

  @Mock private SongApi songApi;
  @Mock private CustomRestClientConfig customRestClientConfig;

  @Override
  protected ClientMain getClientMain() {
    // Needs to be a new instance, to avoid appending status
    return new ClientMain(customRestClientConfig, songApi, new ManifestClient(songApi));
  }

  @Test
  public void testExitCodeOnError() {
    when(songApi.isAlive()).thenThrow(new IllegalStateException("a test error"));
    val e1 = executeMain("ping");
    assertTrue(getExitCode() == 1);
  }
}
