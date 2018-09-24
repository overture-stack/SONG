/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package bio.overture.song.server.validation;

import com.networknt.schema.ValidationMessage;
import lombok.Data;

import java.util.Set;

import static java.util.stream.Collectors.joining;

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
