/*
 * Copyright (c) 2019. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package bio.overture.song.server.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import bio.overture.song.server.service.auth.KeycloakAuthorizationService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.shaded.com.google.common.collect.Sets;

@SpringBootTest
public class TestScopeStrategy {

  @Autowired private KeycloakAuthorizationService keycloakAuthorizationService;

  private final SystemSecurity SYSTEM_SECURITY =
      SystemSecurity.builder().systemScope("song.READ").build();
  private static final StudySecurity STUDY_SECURITY1 =
      StudySecurity.builder()
          .studyPrefix("song.")
          .studySuffix(".READ")
          .systemScope("song.READ")
          .build();

  private static final StudySecurity STUDY_SECURITY2 =
      StudySecurity.builder()
          .studyPrefix("PROGRAMDATA-")
          .studySuffix(".READ")
          .systemScope("song.READ")
          .build();

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
    assertTrue(testStudyVerify2("abc123", "PROGRAMDATA-abc123.READ"));
    assertTrue(testStudyVerify2("abc123", "song.READ"));
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

  private boolean testSystemVerify(String... grantedScopes) {
    return SYSTEM_SECURITY.verifyOneOfSystemScope(Sets.newHashSet(grantedScopes));
  }

  private static boolean testStudyVerify1(String studyId, String... grantedScopes) {
    return STUDY_SECURITY1.verifyOneOfStudyScope(Sets.newHashSet(grantedScopes), studyId);
  }

  private static boolean testStudyVerify2(String studyId, String... grantedScopes) {
    return STUDY_SECURITY2.verifyOneOfStudyScope(Sets.newHashSet(grantedScopes), studyId);
  }
}
