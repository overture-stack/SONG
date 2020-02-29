package bio.overture.song.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

public interface RemoteVerificationService extends VerificationService {
  List<String> getReply(String request);

  default List<String> verify(JsonNode jsonNode) {
    return getReply(jsonNode.toString());
  }
}
