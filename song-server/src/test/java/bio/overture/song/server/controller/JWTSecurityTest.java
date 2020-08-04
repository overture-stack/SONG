package bio.overture.song.server.controller;

import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.model.JWTApplication;
import bio.overture.song.server.model.JWTUser;
import bio.overture.song.server.model.entity.Study;
import bio.overture.song.server.service.StudyService;
import bio.overture.song.server.utils.EndpointTester;
import bio.overture.song.server.utils.generator.StudyGenerator;
import bio.overture.song.server.utils.jwt.JWTGenerator;
import bio.overture.song.server.utils.web.ResponseOption;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.concurrent.TimeUnit.DAYS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static bio.overture.song.core.utils.JsonUtils.convertValue;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.utils.EndpointTester.createEndpointTester;
import static bio.overture.song.server.utils.generator.StudyGenerator.createStudyGenerator;
import static bio.overture.song.server.utils.jwt.JWTGenerator.DEFAULT_ID;
import static bio.overture.song.server.utils.jwt.JWTGenerator.DEFAULT_ISSUER;
import static bio.overture.song.server.utils.jwt.JWTGenerator.DEFAULT_SUBJECT;

@Slf4j
@SpringBootTest
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles({"test", "secure", "jwt"})
public class JWTSecurityTest {

  private static final boolean ENABLE_LOGGING = false;
  private static final RandomGenerator RANDOM_GENERATOR = createRandomGenerator(JWTSecurityTest.class.getSimpleName());

  @Autowired private JWTGenerator jwtGenerator;
  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private StudyService studyService;

  @Getter private EndpointTester endpointTester;
  private MockMvc mockMvc;
  private StudyGenerator studyGenerator;

  @Before
  public void beforeEachTest() {
    if (mockMvc == null){
      this.mockMvc =
          MockMvcBuilders.webAppContextSetup(webApplicationContext)
              .apply(springSecurity())
              .build();
      this.endpointTester = createEndpointTester(mockMvc, ENABLE_LOGGING);
      this.studyGenerator = createStudyGenerator(studyService, RANDOM_GENERATOR);
    }
  }


  @Test
  public void validUserJWTFormat_nonExpired_success(){
    val scopes = Set.of("score.WRITE", "song.WRITE");
    val userContext = generateUserContext(scopes);
    val userJwt = jwtGenerator.generateValidUserJwt(userContext);
    val claims = jwtGenerator.verifyAndGetClaims(userJwt).getBody();
    validateNonTimeClaims(userContext, claims);

    val now = new Date();
    assertTrue(claims.getIssuedAt().before(now));
    assertTrue(claims.getExpiration().after(now));
  }

  @Test
  public void validAppJWTFormat_nonExpired_success(){
    val scopes = Set.of("score.WRITE", "song.WRITE");
    val appContext = generateAppContext(scopes);
    val appJwt = jwtGenerator.generateValidAppJwt(appContext);
    val claims = jwtGenerator.verifyAndGetClaims(appJwt).getBody();
    validateNonTimeClaims(appContext, claims);

    val now = new Date();
    assertTrue(claims.getIssuedAt().before(now));
    assertTrue(claims.getExpiration().after(now));
  }

  @Test
  public void validUserJWTFormat_expired_success(){
    val scopes = Set.of("score.WRITE", "song.WRITE");
    val userContext = generateUserContext(scopes);
    val userJwt = jwtGenerator.generateExpiredUserJwt(userContext);
    val claims = validateExpiredAndGetClaims(userJwt);

    validateNonTimeClaims(userContext, claims);

    val now = new Date();
    assertTrue(claims.getIssuedAt().before(now));
    assertTrue(claims.getExpiration().before(now));
    assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
  }

  @Test
  public void validAppJWTFormat_expired_success(){
    val scopes = Set.of("score.WRITE", "song.WRITE");
    val appContext = generateAppContext(scopes);
    val appJwt = jwtGenerator.generateExpiredAppJwt(appContext);
    val claims = validateExpiredAndGetClaims(appJwt);

    validateNonTimeClaims(appContext, claims);

    val now = new Date();
    assertTrue(claims.getIssuedAt().before(now));
    assertTrue(claims.getExpiration().before(now));
    assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
  }


  @Test
  public void testRob() {
    //    val userContext = generateUserContext("song.WRITE", "score.WRITE");
    val userContext = generateUserContext(Set.of("score.WRITE"));
    val userJwt = jwtGenerator.generateValidUserJwt(userContext);
    createAuthRequestAnd(userJwt)
        .assertOk();
  }

  private Claims validateExpiredAndGetClaims(String jwtString){
    try{
      jwtGenerator.verifyAndGetClaims(jwtString).getBody();
      fail();
    } catch (ExpiredJwtException e){
      return e.getClaims();
    }
    return null;
  }

  private ResponseOption createAuthRequestAnd(String jwt){
    val nonExistentStudyId = studyGenerator.generateNonExistingStudyId();
    val study = Study.builder().studyId(nonExistentStudyId).build();
    return getEndpointTester()
        .initWebRequest()
        .endpoint("/studies/%s/", study.getStudyId())
        .body(study)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
        .postAnd();
  }

  @SuppressWarnings("unchecked")
  private static void validateNonTimeClaims(JWTGenerator.ApplicationContext expectedAppContext, Claims actualClaims){
    assertEmpty(actualClaims.getAudience());
    assertEquals(DEFAULT_ISSUER, actualClaims.getIssuer());
    assertEquals(DEFAULT_ID, actualClaims.getId());
    assertEquals(DEFAULT_SUBJECT, actualClaims.getSubject());
    assertTrue(actualClaims.containsKey("context"));
    val contextMap = (Map<String, Object>)actualClaims.get("context");

    // Validate user data
    assertTrue(contextMap.containsKey("application"));
    val actualApplication = convertValue(contextMap.get("application"), JWTApplication.class);
    assertEquals(expectedAppContext.getApplication(), actualApplication);

    assertTrue(contextMap.containsKey("scope"));
    val actualScopes = (Collection<String>)contextMap.get("scope");
    assertEquals(new ArrayList<>(expectedAppContext.getScope()), actualScopes);
  }

  @SuppressWarnings("unchecked")
  private static void validateNonTimeClaims(JWTGenerator.UserContext expectedUserContext, Claims actualClaims){
    assertEmpty(actualClaims.getAudience());
    assertEquals(DEFAULT_ISSUER, actualClaims.getIssuer());
    assertEquals(DEFAULT_ID, actualClaims.getId());
    assertEquals(DEFAULT_SUBJECT, actualClaims.getSubject());
    assertTrue(actualClaims.containsKey("context"));
    val contextMap = (Map<String, Object>)actualClaims.get("context");

    // Validate user data
    assertTrue(contextMap.containsKey("user"));
    val actualUser = convertValue(contextMap.get("user"), JWTUser.class);
    assertEquals(expectedUserContext.getUser(), actualUser);

    assertTrue(contextMap.containsKey("scope"));
    val actualScopes = (Collection<String>)contextMap.get("scope");
    assertEquals(new ArrayList<>(expectedUserContext.getScope()), actualScopes);
  }

  private static void assertNotEmpty(String value){
    assertFalse(isNullOrEmpty(value));
  }

  private static void assertEmpty(String value){
    assertTrue(isNullOrEmpty(value));
  }


  private static JWTGenerator.ApplicationContext generateAppContext(Collection<String> scopes) {
    return JWTGenerator.ApplicationContext.builder()
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

  private static JWTGenerator.UserContext generateUserContext(Collection<String> scopes) {
    return JWTGenerator.UserContext.builder()
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

}
