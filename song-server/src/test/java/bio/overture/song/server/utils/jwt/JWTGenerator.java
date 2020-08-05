package bio.overture.song.server.utils.jwt;

import static bio.overture.song.core.utils.JsonUtils.toJson;
import static bio.overture.song.core.utils.JsonUtils.toMap;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static java.util.Objects.isNull;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static org.mockito.Mockito.verify;

import bio.overture.song.server.model.JWTApplication;
import bio.overture.song.server.model.JWTUser;
import com.fasterxml.jackson.annotation.JsonInclude;
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
    return generateJwtWithContext(userContext,false);
  }

  public String generateValidAppJwt(@NonNull ApplicationContext applicationContext) {
    return generateJwtWithContext(applicationContext, false);
  }

  public String generateExpiredUserJwt(@NonNull UserContext userContext) {
    return generateJwtWithContext(userContext,true);
  }

  public String generateExpiredAppJwt(@NonNull ApplicationContext applicationContext) {
    return generateJwtWithContext(applicationContext, true);
  }

  public String generateJwtNoContext( boolean expired){
    return generate(calcTTLMs(expired), null);
  }

  public String generateJwtWithContext(@NonNull Object context, boolean expired){
    return generate(calcTTLMs(expired), context);
  }

  @SneakyThrows
  public Jws<Claims> verifyAndGetClaims(String jwtString) {
    val publicKey = keyPair.getPublic();
    return Jwts.parser().setSigningKey(publicKey).parseClaimsJws(jwtString);
  }

  public static ApplicationContext generateDummyAppContext(Collection<String> scopes) {
    return ApplicationContext.builder()
        .scope(scopes)
        .application(
            JWTApplication.builder()
                .name("my-example-application")
                .status("APPROVED")
                .clientId(UUID.randomUUID().toString())
                .type("ADMIN")
                .build())
        .build();
  }

  public static UserContext generateDummyUserContext(Collection<String> scopes) {
    return UserContext.builder()
        .scope(scopes)
        .user(
            JWTUser.builder()
                .email("john.doe@example.com")
                .name("john.doe@example.com")
                .status("APPROVED")
                .firstName("John")
                .lastName("Doe")
                .createdAt(System.currentTimeMillis() - DAYS.toMillis(1))
                .preferredLanguage("ENGLISH")
                .type("ADMIN")
                .build())
        .build();
  }

  private static long calcTTLMs(boolean expired){
    return expired ? 0 : HOURS.toMillis(5);
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
    val jwtBuilder = Jwts.builder()
        .setId(DEFAULT_ID)
        .setIssuedAt(new Date(nowMs))
        .setSubject(DEFAULT_SUBJECT)
        .setIssuer(DEFAULT_ISSUER)
        .setExpiration(new Date(expiry))
        .signWith(SIGNATURE_ALGORITHM, decodedPrivateKey);
    if (!isNull(context)){
        jwtBuilder.addClaims(toMap(toJson(new JwtContext(context))));
    }
    return jwtBuilder.compact();
  }

  @Data
  @AllArgsConstructor
  @JsonInclude(NON_EMPTY)
  public static class JwtContext {
    private Object context;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(NON_EMPTY)
  public static class UserContext {
    private Collection<String> scope;
    private JWTUser user;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(NON_EMPTY)
  public static class ApplicationContext {
    private Collection<String> scope;
    private JWTApplication application;
  }
}
