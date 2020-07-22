package bio.overture.song.server.model;

import lombok.Data;

@Data
public class JWTApplication {
  private String name;
  private String clientId;
  private String status;
  private String type;
}
