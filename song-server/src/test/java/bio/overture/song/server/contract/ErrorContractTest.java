package bio.overture.song.server.contract;

import bio.overture.song.server.utils.contract.JsonNormalizer;
import lombok.val;
import org.junit.jupiter.api.Test;

/**
 * Locks the status, Content-Type and body of each kind of error response with golden files under
 * {@code golden/error}.
 *
 * <p>Song errors come from {@code ServerExceptionHandler}. Errors raised by Spring MVC itself (405,
 * 415, unreadable body, type mismatch) go through the {@code ResponseEntityExceptionHandler} base
 * class, and unknown routes through Boot's {@code /error} page. Spring 6 changes the last two
 * groups to ProblemDetail bodies, so these files record the shapes clients see today.
 */
public class ErrorContractTest extends AbstractContractTest {

  private static final String SEEDED_STUDY_ID = "ABC123";

  /** The stack trace and the epoch-millisecond timestamp of a Song error change on every call. */
  private JsonNormalizer createErrorNormalizer() {
    return createNormalizer().ignoreField("stackTrace").ignoreField("timestamp");
  }

  @Test
  public void serverException_analysisNotFound() {
    assertGolden(
        "error/server-exception-analysis-not-found",
        client.get("/studies/" + SEEDED_STUDY_ID + "/analysis/NO-SUCH-ANALYSIS"),
        createErrorNormalizer());
  }

  @Test
  public void serverException_studyNotFound() {
    assertGolden(
        "error/server-exception-study-not-found",
        client.get("/studies/NO-SUCH-STUDY"),
        createErrorNormalizer());
  }

  @Test
  public void serverException_malformedParameter() {
    assertGolden(
        "error/server-exception-malformed-parameter",
        client.get("/studies/" + SEEDED_STUDY_ID + "/analysis?analysisStates=NOT_A_STATE"),
        createErrorNormalizer());
  }

  @Test
  public void validationFailure_schemaViolation() {
    val payload = readVariantCallPayload(SEEDED_STUDY_ID);
    payload.remove("files");
    assertGolden(
        "error/validation-failure-schema-violation",
        client.post("/submit/" + SEEDED_STUDY_ID, payload.toString()),
        createErrorNormalizer());
  }

  @Test
  public void unknownRoute() {
    assertGolden("error/unknown-route", client.get("/no-such-route"), createErrorNormalizer());
  }

  @Test
  public void unknownRouteUnderStudies() {
    assertGolden(
        "error/unknown-route-under-studies",
        client.get("/studies/" + SEEDED_STUDY_ID + "/no-such-route/x/y"),
        createErrorNormalizer());
  }

  @Test
  public void methodNotAllowed() {
    assertGolden("error/method-not-allowed", client.delete("/isAlive"), createErrorNormalizer());
  }

  @Test
  public void malformedJsonBody() {
    assertGolden(
        "error/malformed-json-body",
        client.post("/studies/" + SEEDED_STUDY_ID + "/analysis/search/id", "{not json"),
        createErrorNormalizer());
  }

  @Test
  public void missingBody() {
    assertGolden(
        "error/missing-body",
        client.send(
            "POST",
            "/studies/" + SEEDED_STUDY_ID + "/analysis/search/id",
            "application/json",
            null),
        createErrorNormalizer());
  }

  @Test
  public void unsupportedMediaType() {
    assertGolden(
        "error/unsupported-media-type",
        client.send(
            "POST",
            "/submit/" + SEEDED_STUDY_ID,
            "text/plain",
            readVariantCallPayload(SEEDED_STUDY_ID).toString()),
        createErrorNormalizer());
  }

  @Test
  public void parameterTypeMismatch() {
    assertGolden(
        "error/parameter-type-mismatch",
        client.get("/studies/" + SEEDED_STUDY_ID + "/analysis/paginated?limit=abc"),
        createErrorNormalizer());
  }

  @Test
  public void notAcceptable() {
    assertGolden(
        "error/not-acceptable",
        client.send("GET", "/studies/" + SEEDED_STUDY_ID, null, null, "application/xml"),
        createErrorNormalizer());
  }
}
