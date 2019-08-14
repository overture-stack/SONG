package bio.overture.song.server.security;

import org.junit.Test;
import org.testcontainers.shaded.com.google.common.collect.Sets;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestScopeStrategy {

  private static final ScopeValidator SCOPE_VALIDATOR = ScopeValidator.builder()
      .scopePrefix("song")
      .scopeSuffix("READ")
      .build();

  @Test
  public void testStudyVerify() {

    // same case matches for user scope and system scope
    assertTrue(testStudyVerify( "ABC123","song.ABC123.READ"));
    assertTrue(testStudyVerify( "ABC123", "song.READ"));
    assertTrue(testStudyVerify( "abc123", "song.abc123.READ"));
    assertTrue(testStudyVerify("abc123","song.READ"));
    // wrong case doesn't match
    assertFalse(testStudyVerify("abc123", "song.ABC123.READ"));
    assertFalse(testStudyVerify("ABC123", "song.abc123.READ"));
    assertFalse(testStudyVerify("abc123","SONG.abc123.READ"));
    assertFalse(testStudyVerify( "abc123", "song.abc123.read"));
    assertFalse(testStudyVerify( "abc123", "song.read"));
    assertFalse(testStudyVerify( "abc123", "SONG.READ"));
  }

  @Test
  public void testSystemVerify() {
    assertFalse(testSystemVerify( "song.abc123.read"));
    assertFalse(testSystemVerify( "song.abc123.READ"));
    assertFalse(testSystemVerify( "song.ABC123.read"));
    assertFalse(testSystemVerify( "song.ABC123.READ"));
    assertFalse(testSystemVerify( "SONG.abc123.read"));
    assertFalse(testSystemVerify( "SONG.abc123.READ"));
    assertFalse(testSystemVerify( "SONG.ABC123.read"));
    assertFalse(testSystemVerify( "SONG.ABC123.READ"));

    assertFalse(testSystemVerify(  "song.read"));
    assertTrue(testSystemVerify(   "song.READ"));
    assertFalse(testSystemVerify(  "SONG.read"));
    assertFalse(testSystemVerify(  "SONG.READ"));
  }

  private boolean testSystemVerify(String... grantedScopes) {
    return SCOPE_VALIDATOR.verifyOneOfSystemScope(Sets.newHashSet(grantedScopes));
  }

  private boolean testStudyVerify(String studyId, String... grantedScopes) {
    return SCOPE_VALIDATOR.verifyOneOfStudyScope(Sets.newHashSet(grantedScopes), studyId);
  }
}
