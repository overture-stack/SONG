package bio.overture.song.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

/**
 * VerificationService checks for issues with a given JSON object that can't be represented with a
 * JSON Schema
 */
public interface VerificationService {
  List<String> verify(JsonNode jsonNode);
}
