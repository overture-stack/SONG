package bio.overture.song.server.service;

import bio.overture.song.server.model.dto.VerifierReply;

public abstract class RemoteVerificationService extends VerificationService {
  abstract VerifierReply getReply(String request);

  public VerifierReply verify(String json) {
    return getReply(json);
  }
}
