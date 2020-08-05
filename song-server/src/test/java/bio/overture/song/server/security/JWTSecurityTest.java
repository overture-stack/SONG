package bio.overture.song.server.security;

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
import bio.overture.song.server.utils.jwt.JWTGenerator.ApplicationContext;
import bio.overture.song.server.utils.jwt.JWTGenerator.UserContext;
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
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static bio.overture.song.core.exceptions.ServerErrors.FORBIDDEN_TOKEN;
import static bio.overture.song.core.utils.JsonUtils.convertValue;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.security.JWTSecurityTest.ContextOptions.APP;
import static bio.overture.song.server.security.JWTSecurityTest.ContextOptions.MISSING_CONTEXT;
import static bio.overture.song.server.security.JWTSecurityTest.ContextOptions.MISSING_PRINCIPLE;
import static bio.overture.song.server.security.JWTSecurityTest.ContextOptions.USER;
import static bio.overture.song.server.security.JWTSecurityTest.RandomContext.createRandomContext;
import static bio.overture.song.server.security.JWTSecurityTest.ScopeOptions.EMPTY_SCOPE;
import static bio.overture.song.server.security.JWTSecurityTest.ScopeOptions.INVALID_STUDY;
import static bio.overture.song.server.security.JWTSecurityTest.ScopeOptions.INVALID_SYSTEM;
import static bio.overture.song.server.security.JWTSecurityTest.ScopeOptions.VALID_STUDY;
import static bio.overture.song.server.security.JWTSecurityTest.ScopeOptions.VALID_SYSTEM;
import static bio.overture.song.server.utils.EndpointTester.createEndpointTester;
import static bio.overture.song.server.utils.generator.StudyGenerator.createStudyGenerator;
import static bio.overture.song.server.utils.jwt.JWTGenerator.DEFAULT_ID;
import static bio.overture.song.server.utils.jwt.JWTGenerator.DEFAULT_ISSUER;
import static bio.overture.song.server.utils.jwt.JWTGenerator.DEFAULT_SUBJECT;
import static bio.overture.song.server.utils.jwt.JWTGenerator.generateDummyAppContext;
import static bio.overture.song.server.utils.jwt.JWTGenerator.generateDummyUserContext;

/**
 * Test JWT authorization using controller interaction with Spring Security loaded.
 * This is more like the controller tests located in the controller package.
 * This method was chosen to ensure Spring Security loads all the correct modules
 */
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
    val userContext = generateDummyUserContext(scopes);
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
    val appContext = generateDummyAppContext(scopes);
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
    val userContext = generateDummyUserContext(scopes);
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
    val appContext = generateDummyAppContext(scopes);
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
  public void authorizedRequest_validUserExpired_Forbidden(){
    runForbiddenErrorTest(USER, VALID_SYSTEM, true);
    runForbiddenErrorTest(USER, VALID_STUDY, true);
  }

  @Test
  public void authorizedRequest_validApplicationNonExpired_Success(){
    runSuccessTest(APP, VALID_SYSTEM, false);
    runSuccessTest(APP, VALID_STUDY, false);
  }

  @Test
  public void authorizedRequest_validApplicationExpired_Forbidden(){
    runForbiddenErrorTest(APP, VALID_SYSTEM, true);
    runForbiddenErrorTest(APP, VALID_STUDY, true);
  }

  // Test where context is not user or application
  @Test
  public void authorizedRequest_missingContextNonExpired_Forbidden(){
    runForbiddenErrorTest(MISSING_CONTEXT, VALID_SYSTEM, false);
  }

  @Test
  public void authorizedRequest_missingPrincipleNonExpired_Forbidden(){
    runForbiddenErrorTest(MISSING_PRINCIPLE, VALID_SYSTEM, false);
    runForbiddenErrorTest(MISSING_PRINCIPLE, VALID_STUDY, false);
    runForbiddenErrorTest(MISSING_PRINCIPLE, INVALID_SYSTEM, false);
    runForbiddenErrorTest(MISSING_PRINCIPLE, INVALID_STUDY, false);
    runForbiddenErrorTest(MISSING_PRINCIPLE, EMPTY_SCOPE, false);
  }

  @Test
  public void authorizedRequest_missingScopeNonExpired_Forbidden(){
    runForbiddenErrorTest(USER, EMPTY_SCOPE, false);
    runForbiddenErrorTest(APP, EMPTY_SCOPE, false);
  }

  @Test
  public void authorizedRequest_missingContextExpired_Forbidden(){
    runForbiddenErrorTest(MISSING_CONTEXT, VALID_SYSTEM, true);
    runForbiddenErrorTest(MISSING_CONTEXT, INVALID_SYSTEM, true);
  }

  @Test
  public void authorizedRequest_missingPrincipleExpired_Forbidden(){
    runForbiddenErrorTest(MISSING_PRINCIPLE, VALID_SYSTEM, true);
    runForbiddenErrorTest(MISSING_PRINCIPLE, VALID_STUDY, true);
    runForbiddenErrorTest(MISSING_PRINCIPLE, INVALID_SYSTEM, true);
    runForbiddenErrorTest(MISSING_PRINCIPLE, INVALID_STUDY, true);
    runForbiddenErrorTest(MISSING_PRINCIPLE, EMPTY_SCOPE, true);
  }

  @Test
  public void authorizedRequest_missingScopeExpired_Forbidden(){
    runForbiddenErrorTest(APP, EMPTY_SCOPE, true);
    runForbiddenErrorTest(USER, EMPTY_SCOPE, true);
  }

  @Test
  public void authorizedRequest_invalidUserNonExpired_Forbidden(){
    runForbiddenErrorTest(USER, INVALID_SYSTEM, false);
    runForbiddenErrorTest(USER, INVALID_STUDY, false);
  }

  @Test
  public void authorizedRequest_invalidUserExpired_Forbidden(){
    runForbiddenErrorTest(USER, INVALID_SYSTEM, true);
    runForbiddenErrorTest(USER, INVALID_STUDY, true);
  }

  @Test
  public void authorizedRequest_invalidApplicationNonExpired_Forbidden(){
    runForbiddenErrorTest(APP, INVALID_SYSTEM, false);
    runForbiddenErrorTest(APP, INVALID_STUDY, false);
  }

  @Test
  public void authorizedRequest_invalidApplicationExpired_Forbidden(){
    runForbiddenErrorTest(APP, INVALID_SYSTEM, true);
    runForbiddenErrorTest(APP, INVALID_STUDY, true);
  }

  @Test
  public void authorizedRequest_malformedAccessToken_Unauthorized(){
    val studyId = studyGenerator.generateNonExistingStudyId();
    createAuthRequestAnd(null, studyId)
        .assertStatusCode(UNAUTHORIZED);

    createAuthRequestAnd("", studyId)
        .assertStatusCode(UNAUTHORIZED);

    createAuthRequestAnd("non-jwt", studyId)
        .assertStatusCode(UNAUTHORIZED);
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
      function = JWTGenerator::generateDummyUserContext;
    } else if (contextOption == APP){
      function = JWTGenerator::generateDummyAppContext;
    } else if (contextOption == MISSING_PRINCIPLE){
      context = createRandomContext();
    } else if (contextOption == MISSING_CONTEXT){
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
      }else if (scopeOptions == EMPTY_SCOPE){
        context = function.apply(List.of());
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

  private void runForbiddenErrorTest(ContextOptions contextOption, ScopeOptions scopeOptions, boolean expired){
    runErrorTest(contextOption, scopeOptions, expired, FORBIDDEN_TOKEN.getHttpStatus());
  }

  /**
   * Note: Cannot use regular fluent way of asserting server exceptions, since its seems ControllerAdvice is not registered.
   * This means, there is no wrapping or exceptions, and so they need to be caught manually. Testing of controller advice
   * is out of scope for this test.
   */
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
      } catch (ServerException e){
        assertEquals(format("expected '%s' httpCode, but actual httpCode was %s", expectedHttpStatus.value(), e.getSongError().getHttpStatusCode()),
            expectedHttpStatus.value(), e.getSongError().getHttpStatusCode());
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
        .header(AUTHORIZATION, "Bearer " + jwt)
        .postAnd();
  }

  @SuppressWarnings("unchecked")
  private static void validateNonTimeClaims(ApplicationContext expectedAppContext, Claims actualClaims){
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
  private static void validateNonTimeClaims(UserContext expectedUserContext, Claims actualClaims){
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
    USER,APP, MISSING_PRINCIPLE, MISSING_CONTEXT;
  }

  enum ScopeOptions{
    VALID_SYSTEM,
    VALID_STUDY,
    INVALID_SYSTEM,
    INVALID_STUDY,
    EMPTY_SCOPE;
  }

}
