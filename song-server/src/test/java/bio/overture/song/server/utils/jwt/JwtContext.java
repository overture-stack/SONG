package bio.overture.song.server.utils.jwt;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Collection;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_EMPTY)
public class JwtContext {

  private JwtScope context;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(NON_EMPTY)
  public static class JwtScope {
    private Collection<String> scope;
  }

  public static JwtContext buildJwtContext(@NonNull Collection<String> scopes){
    return JwtContext.builder()
        .context(JwtScope.builder()
            .scope(scopes)
            .build())
        .build();
  }
}
