package bio.overture.song.server.service;

import bio.overture.song.server.model.dto.VerifierReply;

public interface RemoteVerificationService extends VerificationService {
  VerifierReply getReply(String request);

  default VerifierReply verify(String json) {
    return getReply(json);
  }
}
