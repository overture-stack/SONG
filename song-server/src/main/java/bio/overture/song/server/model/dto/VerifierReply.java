package bio.overture.song.server.model.dto;

import bio.overture.song.server.model.enums.VerifierStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class VerifierReply  {
  VerifierStatus status;
  List<String> details;
}

