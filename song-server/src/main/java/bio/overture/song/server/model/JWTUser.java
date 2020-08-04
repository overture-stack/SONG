package bio.overture.song.server.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JWTUser {
  private String name;
  private String email;
  private String status;
  private String firstName;
  private String lastName;
  private long createdAt;
  private long lastLogin;
  private String preferredLanguage;
  private String type;
  private List<String> groups;
}
