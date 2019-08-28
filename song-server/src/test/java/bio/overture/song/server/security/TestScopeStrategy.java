package bio.overture.song.server.security;

import org.junit.Test;
import org.testcontainers.shaded.com.google.common.collect.Sets;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestScopeStrategy {

  private static final StudyScopeMatcher STUDY_SCOPE_MATCHER1 =
      StudyScopeMatcher.builder()
          .prefix("song")
          .firstDelimiter(".")
          .secondDelimiter(".")
          .suffix("READ")
          .build();

  private static final StudyScopeMatcher STUDY_SCOPE_MATCHER2 =
      StudyScopeMatcher.builder()
          .prefix("PROGRAMDATA")
          .firstDelimiter("-")
          .secondDelimiter(".")
          .suffix("READ")
          .build();

  private static final SystemScopeMatcher SYSTEM_SCOPE_MATCHER =
      SystemScopeMatcher.builder().prefix("song").suffix("READ").build();

  private static final StudySecurity STUDY_SECURITY1 =
      new StudySecurity(STUDY_SCOPE_MATCHER1, SYSTEM_SCOPE_MATCHER);

  private static final StudySecurity STUDY_SECURITY2 =
      new StudySecurity(STUDY_SCOPE_MATCHER2, SYSTEM_SCOPE_MATCHER);

  private static final SystemSecurity SYSTEM_SECURITY = new SystemSecurity(SYSTEM_SCOPE_MATCHER);

  @Test
  public void testStudyVerify1() {

    // same case matches for user scope and system scope
    assertTrue(testStudyVerify1("ABC123", "song.ABC123.READ"));
    assertTrue(testStudyVerify1("ABC123", "song.READ"));
    assertTrue(testStudyVerify1("abc123", "song.abc123.READ"));
    assertTrue(testStudyVerify1("abc123", "song.READ"));
    // wrong case doesn't match
    assertFalse(testStudyVerify1("abc123", "song.ABC123.READ"));
    assertFalse(testStudyVerify1("ABC123", "song.abc123.READ"));
    assertFalse(testStudyVerify1("abc123", "SONG.abc123.READ"));
    assertFalse(testStudyVerify1("abc123", "song.abc123.read"));
    assertFalse(testStudyVerify1("abc123", "song.read"));
    assertFalse(testStudyVerify1("abc123", "SONG.READ"));
  }

  @Test
  public void testStudyVerify2() {

    // same case matches for user scope and system scope
    assertTrue(testStudyVerify2("ABC123", "PROGRAMDATA-ABC123.READ"));
    assertTrue(testStudyVerify2("ABC123", "song.READ"));
    assertTrue(testStudyVerify2("abc123","PROGRAMDATA-abc123.READ"));
    assertTrue(testStudyVerify2("abc123","song.READ"));
    // wrong case doesn't match
    assertFalse(testStudyVerify2("abc123", "PROGRAMDATA-ABC123.READ"));
    assertFalse(testStudyVerify2("ABC123", "PROGRAMDATA-abc123.READ"));
    assertFalse(testStudyVerify2("abc123", "programdata-abc123.READ"));
    assertFalse(testStudyVerify2("abc123", "PROGRAMDATA-abc123.read"));
    assertFalse(testStudyVerify2("abc123", "song.read"));
    assertFalse(testStudyVerify2("abc123", "SONG.READ"));
  }

  @Test
  public void testSystemVerify() {
    assertFalse(testSystemVerify("song.abc123.read"));
    assertFalse(testSystemVerify("song.abc123.READ"));
    assertFalse(testSystemVerify("song.ABC123.read"));
    assertFalse(testSystemVerify("song.ABC123.READ"));
    assertFalse(testSystemVerify("SONG.abc123.read"));
    assertFalse(testSystemVerify("SONG.abc123.READ"));
    assertFalse(testSystemVerify("SONG.ABC123.read"));
    assertFalse(testSystemVerify("SONG.ABC123.READ"));

    assertFalse(testSystemVerify("song.read"));
    assertTrue(testSystemVerify("song.READ"));
    assertFalse(testSystemVerify("SONG.read"));
    assertFalse(testSystemVerify("SONG.READ"));
  }

  private static boolean testSystemVerify(String... grantedScopes) {
    return SYSTEM_SECURITY.verifyOneOfSystemScope(Sets.newHashSet(grantedScopes));
  }

  private static boolean testStudyVerify1(String studyId, String... grantedScopes) {
    return STUDY_SECURITY1.verifyOneOfStudyScope(Sets.newHashSet(grantedScopes), studyId);
  }

  private static boolean testStudyVerify2(String studyId, String... grantedScopes) {
    return STUDY_SECURITY2.verifyOneOfStudyScope(Sets.newHashSet(grantedScopes), studyId);
  }
}
