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
package bio.overture.song.server.service;

import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_ID_COLLISION;
import static bio.overture.song.core.testing.SongErrorAssertions.assertExceptionThrownBy;
import static bio.overture.song.core.testing.SongErrorAssertions.assertSongError;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.service.IdServiceTest.IdServiceResponseTypes.EMPTY;
import static bio.overture.song.server.service.IdServiceTest.IdServiceResponseTypes.MALFORMED_UUID;
import static bio.overture.song.server.service.IdServiceTest.IdServiceResponseTypes.NORMAL;
import static bio.overture.song.server.service.IdServiceTest.IdServiceResponseTypes.WHITESPACE_ONLY;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.OK;

import bio.overture.song.core.utils.RandomGenerator;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.id.client.http.HttpIdClient;
import org.icgc.dcc.id.client.util.HashIdClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpStatus;

@Slf4j
public class IdServiceTest {

  private static final String SUBMITTER_ID_1 = "AN8899";
  private static final String SUBMITTER_ID_2 = "AN112233";

  @Rule public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

  private RandomGenerator randomGenerator;
  private IdService idService;

  @Before
  public void beforeTest() {
    val idServiceUrl = "http://localhost:" + wireMockRule.port();
    val idClient = new HttpIdClient(idServiceUrl, "", "");
    idService = new IdService(idClient);
    randomGenerator = createRandomGenerator(this.getClass().getSimpleName());
  }

  @Test
  public void testObjectId() {
    val analysisId = "AN1";
    val filename = "myfile.bam";

    // Test Normal Case
    responseConfig(NORMAL, analysisId, filename);
    idService.generateFileId(analysisId, filename);

    // Test NotFound case
    responseConfig(IdServiceResponseTypes.NOT_FOUND, analysisId, filename);
    log.info("sdf");
    assertExceptionThrownBy(
        IllegalStateException.class, () -> idService.generateFileId(analysisId, filename));

    // Test Whitespace Case
    responseConfig(WHITESPACE_ONLY, analysisId, filename);
    assertExceptionThrownBy(
        IllegalStateException.class, () -> idService.generateFileId(analysisId, filename));

    // Test Empty Case
    responseConfig(EMPTY, analysisId, filename);
    assertExceptionThrownBy(
        IllegalStateException.class, () -> idService.generateFileId(analysisId, filename));

    // Test Malformed Case
    responseConfig(MALFORMED_UUID, analysisId, filename);
    assertExceptionThrownBy(
        IllegalStateException.class, () -> idService.generateFileId(analysisId, filename));
  }

  enum IdServiceResponseTypes {
    NORMAL,
    MALFORMED_UUID,
    NOT_FOUND,
    EMPTY,
    WHITESPACE_ONLY;
  }

  public String resolveResponse(IdServiceResponseTypes type) {
    switch (type) {
      case NORMAL:
        return randomGenerator.generateRandomUUIDAsString();
      case NOT_FOUND:
        return null;
      case EMPTY:
        return "";
      case MALFORMED_UUID:
        return randomGenerator.generateRandomAsciiString(10);
      case WHITESPACE_ONLY:
        return "   ";
      default:
        throw new IllegalStateException(format("The type '%s' is not supported", type.name()));
    }
  }

  private void responseConfig(IdServiceResponseTypes type, String analysisId, String filename) {
    wireMockRule.resetAll();
    ResponseDefinitionBuilder response;
    if (type != IdServiceResponseTypes.NOT_FOUND) {
      response = aResponse().withStatus(OK.value());
      val body = resolveResponse(type);
      if (!isNull(body)) {
        response.withBody(resolveResponse(type));
      }
    } else {
      response = aResponse().withStatus(HttpStatus.NOT_FOUND.value());
    }
    wireMockRule.stubFor(
        get(urlMatching(format("/object/id\\?analysisId=%s&fileName=%s", analysisId, filename)))
            .willReturn(response));
  }

  @Test
  public void testUndefinedAnalysisId() {
    val idClient = new HashIdClient(true);
    val idService = new IdService(idClient);

    val id1 = idService.resolveAnalysisId("", false);
    assertNotNull(id1);
    assertFalse(idClient.getAnalysisId(id1).isPresent());

    val id1Committed = idService.resolveAndCommitAnalysisId("", false);
    assertNotNull(id1Committed);
    assertTrue(idClient.getAnalysisId(id1Committed).isPresent());

    val id2 = idService.resolveAnalysisId("", false);
    assertNotNull(id2);
    assertNotEquals(id1, id2);
    assertFalse(idClient.getAnalysisId(id2).isPresent());

    val id2Committed = idService.resolveAndCommitAnalysisId("", false);
    assertNotNull(id2Committed);
    assertNotEquals(id1, id2Committed);
    assertTrue(idClient.getAnalysisId(id2Committed).isPresent());

    val id3 = idService.resolveAnalysisId(null, false);
    assertNotNull(id3);
    assertNotEquals(id1, id3);
    assertFalse(idClient.getAnalysisId(id3).isPresent());

    val id3Committed = idService.resolveAndCommitAnalysisId(null, false);
    assertNotNull(id1);
    assertNotEquals(id1, id3Committed);
    assertTrue(idClient.getAnalysisId(id3Committed).isPresent());
  }

  @Test
  public void testAnalysisIdNormal() {
    val idClient = new HashIdClient(true);
    val idService = new IdService(idClient);

    val id1 = idService.resolveAnalysisId(SUBMITTER_ID_1, false);
    assertEquals(id1, SUBMITTER_ID_1);

    val id2 = idService.resolveAnalysisId(SUBMITTER_ID_2, false);
    assertEquals(id2, SUBMITTER_ID_2);
    assertNotEquals(id1, id2);
  }

  @Test
  public void testIgnoreAnalysisIdCollision() {
    val idClient = new HashIdClient(true);
    val idService = new IdService(idClient);

    val id1 = idService.resolveAnalysisId(SUBMITTER_ID_1, false);
    assertEquals(id1, SUBMITTER_ID_1);
    assertFalse(idClient.getAnalysisId(id1).isPresent());

    val id2 = idService.resolveAnalysisId(SUBMITTER_ID_1, true);
    assertEquals(id2, SUBMITTER_ID_1);
    assertEquals(id1, id2);
  }

  @Test
  public void testAnalysisIdCollision() {
    val idClient = new HashIdClient(true);
    val idService = new IdService(idClient);

    val id1 = idService.resolveAndCommitAnalysisId(SUBMITTER_ID_1, false);
    assertEquals(id1, SUBMITTER_ID_1);
    assertSongError(
        () -> idService.resolveAnalysisId(SUBMITTER_ID_1, false),
        ANALYSIS_ID_COLLISION,
        "No exception was thrown, but should have been thrown "
            + "since ignoreAnalysisIdCollisions=false and"
            + " the same id was attempted to be created");

    assertSongError(
        () -> idService.resolveAndCommitAnalysisId(SUBMITTER_ID_1, false),
        ANALYSIS_ID_COLLISION,
        "No exception was thrown, but should have been thrown "
            + "since ignoreAnalysisIdCollisions=false and"
            + " the same id was attempted to be created");

    /*
     * Test that if ignoreAnalysisIdCollisions is true and the analysisId does not exist, the
     * analysisId is still created. SUBMITTER_ID_2 should not exist for first call
     */
    assertFalse(idClient.getAnalysisId(SUBMITTER_ID_2).isPresent());
    val id2 = idService.resolveAndCommitAnalysisId(SUBMITTER_ID_2, true);
    assertEquals(id2, SUBMITTER_ID_2);
    assertSongError(
        () -> idService.resolveAnalysisId(SUBMITTER_ID_2, false),
        ANALYSIS_ID_COLLISION,
        "No exception was thrown, but should have been thrown "
            + "since ignoreAnalysisIdCollisions=false and"
            + " the same id was attempted to be created");
    assertSongError(
        () -> idService.resolveAndCommitAnalysisId(SUBMITTER_ID_2, false),
        ANALYSIS_ID_COLLISION,
        "No exception was thrown, but should have been thrown "
            + "since ignoreAnalysisIdCollisions=false and"
            + " the same id was attempted to be created");
  }
}
