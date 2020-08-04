package bio.overture.song.server.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JWTApplication {
  private String name;
  private String clientId;
  private String status;
  private String type;
}
