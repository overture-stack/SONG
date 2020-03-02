package bio.overture.song.server.service;

import bio.overture.song.server.model.dto.VerifierReply;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

/**
 * VerificationService checks for issues with a given JSON object that can't be represented with a
 * JSON Schema
 */
public interface VerificationService {
  VerifierReply verify(String json);
}
