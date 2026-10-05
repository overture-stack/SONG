package bio.overture.song.server.utils.contract;

import static java.util.Objects.isNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import lombok.Value;
import lombok.val;

/**
 * An HTTP response exactly as the server sent it: status, Content-Type header and body text, with
 * no client-side conversion.
 */
@Value
public class RawResponse {

  /** Plain mapper so parsing does not apply any of the application's Jackson customizations. */
  private static final ObjectMapper PLAIN_MAPPER = new ObjectMapper();

  int status;

  /** The Content-Type header value, or null when the response has none. */
  String contentType;

  /** The body text, or an empty string when the response has no body. */
  String body;

  /**
   * Builds the JSON document that golden files store for a response:
   *
   * <ul>
   *   <li>{@code status}: the numeric status code
   *   <li>{@code contentType}: the Content-Type header, or null
   *   <li>{@code body}: the parsed JSON body; null when the body is empty
   *   <li>{@code bodyText}: in place of {@code body}, the raw text when the body is not JSON
   * </ul>
   */
  public ObjectNode toEnvelope() {
    val envelope = JsonNodeFactory.instance.objectNode();
    envelope.put("status", status);
    envelope.put("contentType", contentType);
    if (isNull(body) || body.isEmpty()) {
      envelope.putNull("body");
    } else {
      try {
        envelope.set("body", PLAIN_MAPPER.readTree(body));
      } catch (IOException exception) {
        envelope.put("bodyText", body);
      }
    }
    return envelope;
  }

  /** Parses the body as JSON; throws when the body is not valid JSON. */
  public JsonNode bodyAsJson() {
    try {
      return PLAIN_MAPPER.readTree(body);
    } catch (IOException exception) {
      throw new IllegalStateException(
          "Response body is not JSON (status " + status + "): " + body, exception);
    }
  }
}
