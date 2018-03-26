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
package org.icgc.dcc.song.server.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.id.client.util.HashIdClient;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.ANALYSIS_ID_COLLISION;
import static org.icgc.dcc.song.core.testing.SongErrorAssertions.assertSongError;

@Slf4j
public class IdServiceTest {

  private static final String SUBMITTER_ID_1 = "AN8899";
  private static final String SUBMITTER_ID_2 = "AN112233";

  @Test
  public void testUndefinedAnalysisId(){
    val idService = createHashIdService();

    val id1 = idService.resolveAnalysisId("",false);
    assertThat(id1).isNotNull();

    val id2 = idService.resolveAnalysisId("",false);
    assertThat(id2).isNotNull();
    assertThat(id1).isNotEqualTo(id2);

    val id3 = idService.resolveAnalysisId(null,false);
    assertThat(id1).isNotNull();
    assertThat(id1).isNotEqualTo(id3);
  }

  @Test
  public void testAnalysisIdNormal(){
    val idService = createHashIdService();

    val id1 = idService.resolveAnalysisId(SUBMITTER_ID_1,false);
    assertThat(id1).isEqualTo(SUBMITTER_ID_1);

    val id2 = idService.resolveAnalysisId(SUBMITTER_ID_2,false);
    assertThat(id2).isEqualTo(SUBMITTER_ID_2);
    assertThat(id1).isNotEqualTo(id2);

  }

  @Test
  public void testIgnoreAnalysisIdCollision(){
    val idService = createHashIdService();

    val id1 = idService.resolveAnalysisId(SUBMITTER_ID_1,false);
    assertThat(id1).isEqualTo(SUBMITTER_ID_1);

    val id2 = idService.resolveAnalysisId(SUBMITTER_ID_1,true);
    assertThat(id2).isEqualTo(SUBMITTER_ID_1);
    assertThat(id1).isEqualTo(id2);
  }

  @Test
  public void testAnalysisIdCollision(){
    val idService = createHashIdService();

    val id1 = idService.resolveAnalysisId(SUBMITTER_ID_1,false);
    assertThat(id1).isEqualTo(SUBMITTER_ID_1);
    assertSongError(
        () -> idService.resolveAnalysisId(SUBMITTER_ID_1,false),
        ANALYSIS_ID_COLLISION,
        "No exception was thrown, but should have been thrown "
            + "since ignoreAnalysisIdCollisions=false and"
        + " the same id was attempted to be created");

    /*
     * Test that if ignoreAnalysisIdCollisions is true and the analysisId does not exist, the
     * analysisId is still created. SUBMITTER_ID_2 should not exist for first call
     */
    val id2 = idService.resolveAnalysisId(SUBMITTER_ID_2,true);
    assertThat(id2).isEqualTo(SUBMITTER_ID_2);
    assertSongError(
        () -> idService.resolveAnalysisId(SUBMITTER_ID_2,false),
        ANALYSIS_ID_COLLISION,
        "No exception was thrown, but should have been thrown "
            + "since ignoreAnalysisIdCollisions=false and"
            + " the same id was attempted to be created");
  }

  private static final IdService createHashIdService(){
    return new IdService(new HashIdClient(true));
  }

}
