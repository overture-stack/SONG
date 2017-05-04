package org.icgc.dcc.sodalite.server.validation;

import java.util.Set;
import static java.util.stream.Collectors.joining;

import com.networknt.schema.ValidationMessage;

import lombok.Data;
import lombok.val;

@Data
public class ValidationResponse {

	private Set<ValidationMessage>	messages;
	private boolean valid = true;
	
	public ValidationResponse(Set<ValidationMessage> messages) {
		this.messages = messages;
		if ((messages != null) && !messages.isEmpty()) {
			valid = false;
		}
	}
	
	public String getValidationErrors() {
		val errorMessages = messages.stream()
				.map(ValidationMessage::getMessage)
				.collect(joining("|"));
		return errorMessages;
	}
}
