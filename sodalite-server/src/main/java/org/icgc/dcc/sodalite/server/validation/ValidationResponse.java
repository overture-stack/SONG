package org.icgc.dcc.sodalite.server.validation;

import java.util.Set;
import static java.util.stream.Collectors.joining;

import com.networknt.schema.ValidationMessage;
import lombok.Data;

@Data
public class ValidationResponse {

  private Set<ValidationMessage> messages;
  private boolean valid = true;

  public String getValidationErrors() {
    return messages.stream()
        .map(ValidationMessage::getMessage)
        .collect(joining("|"));
  }

  ValidationResponse(Set<ValidationMessage> messages) {
    this.messages = messages;
    if ((messages != null) && !messages.isEmpty()) {
      valid = false;
    }
  }

}
