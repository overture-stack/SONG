package bio.overture.song.server.utils.jwt;

import static bio.overture.song.core.utils.JsonUtils.toJson;
import static bio.overture.song.core.utils.JsonUtils.toMap;
import static java.util.concurrent.TimeUnit.HOURS;
import static org.mockito.Mockito.verify;

import bio.overture.song.server.model.JWTApplication;
import bio.overture.song.server.model.JWTUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.KeyPair;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile({"test", "jwt"})
public class JWTGenerator {

  public static final String DEFAULT_ISSUER = "ego";
  public static final String DEFAULT_ID = "68418f9f-65b9-4a17-ac1c-88acd9984fe0";
  public static final String DEFAULT_SUBJECT= "none";
  private static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.RS256;

  private final KeyPair keyPair;

  @Autowired
  public JWTGenerator(@NonNull KeyPair keyPair) {
    this.keyPair = keyPair;
  }

  public String generateValidUserJwt(@NonNull UserContext userContext) {
    return generate(HOURS.toMillis(5), userContext);
  }

  public String generateValidAppJwt(@NonNull ApplicationContext applicationContext) {
    return generate(HOURS.toMillis(5), applicationContext);
  }

  public String generateExpiredUserJwt(@NonNull UserContext userContext) {
    return generate(0, userContext);
  }

  public String generateExpiredAppJwt(@NonNull ApplicationContext applicationContext) {
    return generate(0, applicationContext);
  }

  @SneakyThrows
  public Jws<Claims> verifyAndGetClaims(String jwtString) {
    val publicKey = keyPair.getPublic();
    return Jwts.parser().setSigningKey(publicKey).parseClaimsJws(jwtString);
  }

  @SneakyThrows
  private String generate(long ttlMs, Object context) {
    long nowMs = System.currentTimeMillis();

    long expiry;
    // if ttlMs <= 0 make it expired
    if (ttlMs <= 0) {
      expiry = nowMs - 10000;
      nowMs -= 100000L;
    } else {
      expiry = nowMs + ttlMs;
    }

    val decodedPrivateKey = keyPair.getPrivate();
    val jwtString = Jwts.builder()
        .setId(DEFAULT_ID)
        .setIssuedAt(new Date(nowMs))
        .setSubject(DEFAULT_SUBJECT)
        .setIssuer(DEFAULT_ISSUER)
        .setExpiration(new Date(expiry))
        .addClaims(toMap(toJson(new JwtContext(context))))
        .signWith(SIGNATURE_ALGORITHM, decodedPrivateKey)
        .compact();
    return jwtString;
  }

  @Data
  @AllArgsConstructor
  public static class JwtContext {
    private Object context;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UserContext {
    private Collection<String> scope;
    private JWTUser user;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ApplicationContext {
    private Collection<String> scope;
    private JWTApplication application;
  }
}
