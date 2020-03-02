package bio.overture.song.server.service;

import bio.overture.song.server.model.dto.VerifierReply;
import bio.overture.song.server.model.enums.VerifierStatus;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.List;


import static bio.overture.song.core.exceptions.ServerErrors.*;
import static bio.overture.song.core.exceptions.ServerException.buildServerException;
import static java.lang.String.format;

/**
 * VerificationService checks for issues with a given JSON object that can't be represented with a
 * JSON Schema
 */
@Slf4j
public abstract class VerificationService {
  abstract VerifierReply verify(String json);

  public static void verifyPayload(String payloadString, List<VerificationService> verificationServices) {
    // Verify payload against our external verifier services
    List<String> issues = new ArrayList<>();
    for (val v : verificationServices) {
      if (v == null) {
        log.error("Configuration Error: Null verification service object detected ... skipping it");
        continue;
      }

      val result = v.verify(payloadString);
      val status = result.getStatus();
      if (status.equals(VerifierStatus.VERIFIER_ERROR)) {
        throw buildServerException(VerificationService.class, BAD_REPLY_FROM_GATEWAY, "Verifier threw exception: "+
          result.getDetails().toString());
      } else if(status.equals(VerifierStatus.ISSUES)) {
        issues.addAll(result.getDetails());
      } else if (! status.equals(VerifierStatus.OK)) {
        throw buildServerException(VerificationService.class, UNKNOWN_ERROR,
          "Unhandled enumeration case in verifyPayload");
      }
    }

    if (!issues.isEmpty()) {
      val message = format("Payload verification issues: %s", issues.toString());
      throw buildServerException(VerificationService.class, PAYLOAD_VERIFICATION_FAILED, message);
    }
  }

}
