package bio.overture.song.server.contract;

import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Locks how the server accepts requests and what the write endpoints return, with golden files
 * under {@code golden/request}.
 *
 * <p>Covers the request Content-Type variants clients send and request bodies with unknown fields.
 * Unknown-field handling depends on how Jackson picks the creator for Lombok-generated
 * constructors, which changes between Jackson versions without a compile error.
 */
public class RequestContractTest extends AbstractContractTest {

  private static final String STUDY_ID = "CONTRACT-REQUEST";
  private static final String JSON_WITH_CHARSET = "application/json;charset=UTF-8";

  /** Read by the id search tests and never modified. */
  private String analysisId;

  /** Modified by the file update test. */
  private String fileUpdateAnalysisId;

  @BeforeAll
  public void setUpData() {
    createStudy(STUDY_ID);
    analysisId = submit(STUDY_ID, readVariantCallPayload(STUDY_ID));
    fileUpdateAnalysisId = submit(STUDY_ID, readVariantCallPayload(STUDY_ID));
  }

  @Test
  public void submit_response() {
    assertGolden(
        "request/submit",
        client.post(
            "/submit/" + STUDY_ID + "?allowDuplicates=true",
            readVariantCallPayload(STUDY_ID).toString()),
        createNormalizer());
  }

  @Test
  public void submit_jsonWithCharset() {
    assertGolden(
        "request/submit-json-with-charset",
        client.send(
            "POST",
            "/submit/" + STUDY_ID + "?allowDuplicates=true",
            JSON_WITH_CHARSET,
            readVariantCallPayload(STUDY_ID).toString()),
        createNormalizer());
  }

  @Test
  public void submit_duplicateWithoutAllowDuplicates() {
    assertGolden(
        "request/submit-duplicate",
        client.post("/submit/" + STUDY_ID, readVariantCallPayload(STUDY_ID).toString()),
        createNormalizer().ignoreField("stackTrace").ignoreField("timestamp"));
  }

  @Test
  public void createStudy_response() {
    val study = MAPPER.createObjectNode().put("studyId", "CONTRACT-NEW").put("name", "new");
    assertGolden(
        "request/create-study",
        client.post("/studies/CONTRACT-NEW/", study.toString()),
        createNormalizer());
  }

  @Test
  public void createStudy_jsonWithCharsetAndUnknownField() {
    val study =
        MAPPER
            .createObjectNode()
            .put("studyId", "CONTRACT-CHARSET")
            .put("name", "charset")
            .put("unknownField", "ignored?");
    assertGolden(
        "request/create-study-json-with-charset-unknown-field",
        client.send("POST", "/studies/CONTRACT-CHARSET/", JSON_WITH_CHARSET, study.toString()),
        createNormalizer().ignoreField("stackTrace").ignoreField("timestamp"));
  }

  @Test
  public void idSearch_unknownField() {
    val objectId = readVcfObjectId(STUDY_ID, analysisId);
    val request = MAPPER.createObjectNode().put("objectId", objectId).put("unknownField", 1);
    val normalizer =
        withAnalysisIds(createNormalizer(), "submitted", STUDY_ID, analysisId)
            .ignoreField("stackTrace")
            .ignoreField("timestamp");
    assertGolden(
        "request/id-search-unknown-field",
        client.post("/studies/" + STUDY_ID + "/analysis/search/id", request.toString()),
        normalizer);
  }

  @Test
  public void updateFile_unknownField() {
    val objectId = readVcfObjectId(STUDY_ID, fileUpdateAnalysisId);
    val request = MAPPER.createObjectNode().put("fileMd5sum", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
    request.put("unknownField", true);
    val normalizer =
        withAnalysisIds(createNormalizer(), "updated", STUDY_ID, fileUpdateAnalysisId)
            .ignoreField("stackTrace")
            .ignoreField("timestamp");
    assertGolden(
        "request/update-file-unknown-field",
        client.put("/studies/" + STUDY_ID + "/files/" + objectId, request.toString()),
        normalizer);
  }

  @Test
  public void registerAnalysisType_unknownField() {
    val request = MAPPER.createObjectNode().put("name", "contractType").put("unknownField", "x");
    request
        .putObject("schema")
        .put("type", "object")
        .putObject("properties")
        .putObject("note")
        .put("type", "string");
    assertGolden(
        "request/register-analysis-type-unknown-field",
        client.post("/schemas", request.toString()),
        createNormalizer().ignoreField("stackTrace").ignoreField("timestamp"));
  }
}
