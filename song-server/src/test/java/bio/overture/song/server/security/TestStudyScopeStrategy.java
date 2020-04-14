package bio.overture.song.server.security;

import lombok.val;
import org.junit.Test;
import org.testcontainers.shaded.com.google.common.collect.Sets;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestStudyScopeStrategy {
  @Test
  public void testVerify() {
    val s = new StudyScopeStrategy("song", "READ");

    // same case matches for user scope and system scope
    assertTrue(testVerify(s, "ABC123","song.ABC123.READ"));
    assertTrue(testVerify(s, "ABC123", "song.READ"));

    assertTrue(testVerify(s, "abc123", "song.abc123.READ"));
    assertTrue(testVerify(s,"abc123","song.READ"));

    // wrong case doesn't match
    assertFalse(testVerify(s,"abc123", "song.ABC123.READ"));
    assertFalse(testVerify(s,"ABC123", "song.abc123.READ"));

    assertFalse(testVerify(s,"abc123","SONG.abc123.READ"));
    assertFalse(testVerify(s, "abc123", "song.abc123.read"));
    assertFalse(testVerify(s, "abc123", "song.read"));
    assertFalse(testVerify(s, "abc123", "SONG.READ"));
  }

  private boolean testVerify(StudyScopeStrategy s,  String studyId, String... grantedScopes) {
    return s.verify(Sets.newHashSet(grantedScopes), studyId);
  }
}
