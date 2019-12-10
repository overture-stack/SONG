package bio.overture.song.server.model.dto;

import lombok.NonNull;
import lombok.Value;

@Value
public class GenericMessage {
  @NonNull private final String message;
}
