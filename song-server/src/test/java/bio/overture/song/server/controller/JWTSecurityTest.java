package bio.overture.song.server.controller;

import bio.overture.song.core.exceptions.ServerException;
import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.config.SecurityConfig;
import bio.overture.song.server.model.JWTApplication;
import bio.overture.song.server.model.JWTUser;
import bio.overture.song.server.model.entity.Study;
import bio.overture.song.server.service.StudyService;
import bio.overture.song.server.utils.EndpointTester;
import bio.overture.song.server.utils.generator.StudyGenerator;
import bio.overture.song.server.utils.jwt.JWTGenerator;
import bio.overture.song.server.utils.web.ResponseOption;
import com.google.common.base.Function;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.concurrent.TimeUnit.DAYS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static bio.overture.song.core.exceptions.ServerErrors.UNAUTHORIZED_TOKEN;
import static bio.overture.song.core.utils.JsonUtils.convertValue;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.controller.JWTSecurityTest.ContextOptions.APP;
import static bio.overture.song.server.controller.JWTSecurityTest.ContextOptions.UNKNOWN;
import static bio.overture.song.server.controller.JWTSecurityTest.ContextOptions.USER;
import static bio.overture.song.server.controller.JWTSecurityTest.RandomContext.createRandomContext;
import static bio.overture.song.server.controller.JWTSecurityTest.ScopeOptions.INVALID_STUDY;
import static bio.overture.song.server.controller.JWTSecurityTest.ScopeOptions.INVALID_SYSTEM;
import static bio.overture.song.server.controller.JWTSecurityTest.ScopeOptions.MISSING;
import static bio.overture.song.server.controller.JWTSecurityTest.ScopeOptions.VALID_STUDY;
import static bio.overture.song.server.controller.JWTSecurityTest.ScopeOptions.VALID_SYSTEM;
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

  /**
   * Constants
   */
  private static final boolean ENABLE_LOGGING = false;
  private static final RandomGenerator RANDOM_GENERATOR = createRandomGenerator(JWTSecurityTest.class.getSimpleName());

  /**
   * Dependencies
   */
  @Autowired private JWTGenerator jwtGenerator;
  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private StudyService studyService;
  @Autowired private SecurityConfig securityConfig;

  /**
   * State
   */
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

  /**
   * Validate the JWT format for users on non expired JWTs
   */
  @Test
  public void validUserJWTFormat_nonExpired_success(){
    val scopes = Set.of(resolveSystemScope(), "score.WRITE");
    val userContext = generateUserContext(scopes);
    val userJwt = jwtGenerator.generateValidUserJwt(userContext);
    val claims = jwtGenerator.verifyAndGetClaims(userJwt).getBody();
    validateNonTimeClaims(userContext, claims);

    val now = new Date();
    assertTrue(claims.getIssuedAt().before(now));
    assertTrue(claims.getExpiration().after(now));
  }

  /**
   * Validate the JWT format for applications on non expired JWTs
   */
  @Test
  public void validAppJWTFormat_nonExpired_success(){
    val scopes = Set.of("score.WRITE", resolveSystemScope());
    val appContext = generateAppContext(scopes);
    val appJwt = jwtGenerator.generateValidAppJwt(appContext);
    val claims = jwtGenerator.verifyAndGetClaims(appJwt).getBody();
    validateNonTimeClaims(appContext, claims);

    val now = new Date();
    assertTrue(claims.getIssuedAt().before(now));
    assertTrue(claims.getExpiration().after(now));
  }

  /**
   * Validate the JWT format for users on expired JWTs
   */
  @Test
  public void validUserJWTFormat_expired_success(){
    val scopes = Set.of("score.WRITE", resolveSystemScope());
    val userContext = generateUserContext(scopes);
    val userJwt = jwtGenerator.generateExpiredUserJwt(userContext);
    val claims = validateExpiredAndGetClaims(userJwt);

    validateNonTimeClaims(userContext, claims);

    val now = new Date();
    assertTrue(claims.getIssuedAt().before(now));
    assertTrue(claims.getExpiration().before(now));
    assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
  }

  /**
   * Validate the JWT format for applications on expired JWTs
   */
  @Test
  public void validAppJWTFormat_expired_success(){
    val scopes = Set.of("score.WRITE", resolveSystemScope());
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
  public void authorizedRequest_validUserNonExpired_Success(){
    runSuccessTest(USER, VALID_SYSTEM, false);
    runSuccessTest(USER, VALID_STUDY, false);
  }

  @Test
  public void authorizedRequest_validUserExpired_UnauthorizedToken(){
    runErrorTest(USER, VALID_SYSTEM, true, UNAUTHORIZED_TOKEN.getHttpStatus() );
    runErrorTest(USER, VALID_STUDY, true, UNAUTHORIZED_TOKEN.getHttpStatus() );
  }

  @Test
  public void authorizedRequest_validApplicationNonExpired_Success(){
    runSuccessTest(APP, VALID_SYSTEM, false);
    runSuccessTest(APP, VALID_STUDY, false);
  }

  @Test
  public void authorizedRequest_validApplicationExpired_UnauthorizedToken(){
    runErrorTest(APP, VALID_SYSTEM, true, UNAUTHORIZED_TOKEN.getHttpStatus() );
    runErrorTest(APP, VALID_STUDY, true, UNAUTHORIZED_TOKEN.getHttpStatus() );
  }

  // Test where context is not user or application
  @Test
  public void authorizedRequest_missingContextNonExpired_UnauthorizedToken(){
    runErrorTest(ContextOptions.MISSING, VALID_SYSTEM, false, UNAUTHORIZED_TOKEN.getHttpStatus() );
  }

  @Test
  public void authorizedRequest_missingPrincipleNonExpired_UnauthorizedToken(){
    runErrorTest(UNKNOWN, VALID_SYSTEM, false, UNAUTHORIZED_TOKEN.getHttpStatus() );
    runErrorTest(UNKNOWN, VALID_STUDY, false, UNAUTHORIZED_TOKEN.getHttpStatus() );
    runErrorTest(UNKNOWN, INVALID_SYSTEM, false, UNAUTHORIZED_TOKEN.getHttpStatus() );
    runErrorTest(UNKNOWN, INVALID_STUDY, false, UNAUTHORIZED_TOKEN.getHttpStatus() );
    runErrorTest(UNKNOWN, ScopeOptions.MISSING, false, UNAUTHORIZED_TOKEN.getHttpStatus() );
  }

  @Test
  public void authorizedRequest_missingScopeNonExpired_UnauthorizedToken(){
    runErrorTest(USER, ScopeOptions.MISSING, false, UNAUTHORIZED_TOKEN.getHttpStatus() );
    runErrorTest(APP, ScopeOptions.MISSING, false, UNAUTHORIZED_TOKEN.getHttpStatus() );
  }

  @Test
  public void authorizedRequest_missingContextExpired_UnauthorizedToken(){
    runErrorTest(ContextOptions.MISSING, VALID_SYSTEM, true, UNAUTHORIZED_TOKEN.getHttpStatus() );
    runErrorTest(ContextOptions.MISSING, INVALID_SYSTEM, true, UNAUTHORIZED_TOKEN.getHttpStatus() );
  }

  @Test
  public void authorizedRequest_missingPrincipleExpired_UnauthorizedToken(){
    runErrorTest(UNKNOWN, VALID_SYSTEM, true, UNAUTHORIZED_TOKEN.getHttpStatus() );
    runErrorTest(UNKNOWN, VALID_STUDY, true, UNAUTHORIZED_TOKEN.getHttpStatus() );
    runErrorTest(UNKNOWN, INVALID_SYSTEM, true, UNAUTHORIZED_TOKEN.getHttpStatus() );
    runErrorTest(UNKNOWN, INVALID_STUDY, true, UNAUTHORIZED_TOKEN.getHttpStatus() );
    runErrorTest(UNKNOWN, MISSING, true, UNAUTHORIZED_TOKEN.getHttpStatus() );
  }

  @Test
  public void authorizedRequest_missingScopeExpired_UnauthorizedToken(){
    runErrorTest(APP, MISSING, true, UNAUTHORIZED_TOKEN.getHttpStatus() );
    runErrorTest(USER, MISSING, true, UNAUTHORIZED_TOKEN.getHttpStatus() );
  }

  @Test
  public void authorizedRequest_invalidUserNonExpired_UnauthorizedToken(){
    runErrorTest(USER, INVALID_SYSTEM, false, UNAUTHORIZED_TOKEN.getHttpStatus() );
    runErrorTest(USER, INVALID_STUDY, false, UNAUTHORIZED_TOKEN.getHttpStatus() );
  }

  @Test
  public void authorizedRequest_invalidUserExpired_UnauthorizedToken(){
    runErrorTest(USER, INVALID_SYSTEM, true, UNAUTHORIZED_TOKEN.getHttpStatus() );
    runErrorTest(USER, INVALID_STUDY, true, UNAUTHORIZED_TOKEN.getHttpStatus() );
  }

  @Test
  public void authorizedRequest_invalidApplicationNonExpired_UnauthorizedToken(){
    runErrorTest(APP, INVALID_SYSTEM, false, UNAUTHORIZED_TOKEN.getHttpStatus() );
    runErrorTest(APP, INVALID_STUDY, false, UNAUTHORIZED_TOKEN.getHttpStatus() );
  }

  @Test
  public void authorizedRequest_invalidApplicationExpired_UnauthorizedToken(){
    runErrorTest(APP, INVALID_SYSTEM, true, UNAUTHORIZED_TOKEN.getHttpStatus() );
    runErrorTest(APP, INVALID_STUDY, true, UNAUTHORIZED_TOKEN.getHttpStatus() );
  }

  private String resolveSystemScope(){
    return this.securityConfig.getScope().getSystem();
  }

  private String resolveStudyScope(String studyId){
    val studyScopeConfig = this.securityConfig.getScope().getStudy();
    return studyScopeConfig.getPrefix()+studyId+studyScopeConfig.getSuffix();
  }

  private String generateConstrainedJWTString (ContextOptions contextOption, ScopeOptions scopeOptions, String studyId,  boolean expired){
    val nonExistentStudyId = studyGenerator.generateNonExistingStudyId();
    Object context = null;
    Function<Collection<String>, Object> function = null;
    if (contextOption == USER){
      function = JWTSecurityTest::generateUserContext;
    } else if (contextOption == APP){
      function = JWTSecurityTest::generateAppContext;
    } else if (contextOption == UNKNOWN){
      context = createRandomContext();
    } else if (contextOption == ContextOptions.MISSING){
      context = null;
    } else {
      fail("shouldnt be here");
    }

    if (contextOption == APP || contextOption== USER){
      if (scopeOptions == VALID_SYSTEM){
        context = function.apply(List.of(resolveSystemScope(), "score.WRITE", "id.READ"));
      }else if (scopeOptions == VALID_STUDY){
        context = function.apply(List.of(resolveStudyScope(studyId), "score.WRITE", "id.READ"));
      }else if (scopeOptions == INVALID_STUDY){
        context = function.apply(List.of(resolveStudyScope(nonExistentStudyId), "score.WRITE", "id.READ"));
      }else if (scopeOptions == INVALID_SYSTEM){
        context = function.apply(List.of("song.READ", "id.READ"));
      }else if (scopeOptions == ScopeOptions.MISSING){
        context = null;
      } else{
        fail("shouldnt be here");
      }
    }

    String jwtString = null;
    if (isNull(context)){
      jwtString = jwtGenerator.generateJwtNoContext(expired);
    } else {
      jwtString = jwtGenerator.generateJwtWithContext(context, expired);
    }
    return jwtString;
  }

  private void runSuccessTest(ContextOptions contextOption, ScopeOptions scopeOptions, boolean expired){
    runErrorTest(contextOption, scopeOptions, expired, null);
  }

  private void runErrorTest(ContextOptions contextOption, ScopeOptions scopeOptions, boolean expired, HttpStatus expectedHttpStatus){
    val studyId = studyGenerator.generateNonExistingStudyId();
    val jwtString = generateConstrainedJWTString(contextOption, scopeOptions, studyId, expired);
    if (isNull(expectedHttpStatus)){
      createAuthRequestAnd(jwtString, studyId)
          .assertOk();
    } else {
      try {
       createAuthRequestAnd(jwtString, studyId)
           .assertStatusCode(expectedHttpStatus);
       fail(format("Was expecting an error with httpStatus '%s' but there was no error", expectedHttpStatus));
      } catch (ServerException e){
        assertEquals(format("expected '%s' httpCode, but actual httpCode was %s", expectedHttpStatus.value(), e.getSongError().getHttpStatusCode()),
            expectedHttpStatus.value(), e.getSongError().getHttpStatusCode());
      } catch (HttpClientErrorException e){
       log.info("sdf");
      } catch (Throwable t){
        log.info("sdf");

      }
    }
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

  private ResponseOption createAuthRequestAnd(String jwt, String studyId){
    val study = Study.builder().studyId(studyId).build();
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

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class RandomContext{
    private String randomId;
    private String randomName;

    public static RandomContext createRandomContext(){
      return new RandomContext(UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }
  }

  enum ContextOptions{
    USER,APP,UNKNOWN,MISSING;
  }

  enum ScopeOptions{
    VALID_SYSTEM,
    VALID_STUDY,
    INVALID_SYSTEM,
    INVALID_STUDY,
    MISSING;
  }

}
