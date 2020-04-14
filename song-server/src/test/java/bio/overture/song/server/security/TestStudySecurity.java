package bio.overture.song.server.security;

import bio.overture.song.server.oauth.AccessTokenConverterWithExpiry;
import com.google.common.collect.Sets;
import lombok.val;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;

public class TestStudySecurity {
  String TEST_STUDY = "TEST-CA";

  @Test
  public void testValidStudyScopeOK() {
    val m = new TreeMap<String, Object>();
    m.put("exp", 10);
    m.put("scope", newHashSet("collab.TEST-CA.READ"));
    test_authorize(m, true);
  }

  @Test
  public void testExpiredTokenFails() {
    val m = new TreeMap<String, Object>();
    m.put("exp", 0);
    m.put("scope", newHashSet("collab.TEST-CA.READ"));
    test_authorize(m, false);
  }

  @Test
  public void testUnauthorizedFails() {
    val m = new TreeMap<String, Object>();
    m.put("exp", 10);
    m.put("scope", Collections.emptySet());
    test_authorize(m, false);
  }

  @Test
  public void testValidGlobalScopeOK() {
    val m = new TreeMap<String, Object>();
    m.put("exp", 10);
    m.put("scope", newHashSet("collab.READ"));
    test_authorize(m, true);
  }
  @Test
  public void testValidGlobalScopeFails() {
    val m = new TreeMap<String, Object>();
    m.put("exp", 0);
    m.put("scope", newHashSet("DCC.READ"));
    test_authorize(m, false);
  }

  public void test_authorize(Map<String, ?> map, boolean expected) {
    val prefix = "collab";
    val suffix = "READ";
    val studySecurity = new StudyScopeStrategy(prefix, suffix);
    val authentication = new AccessTokenConverterWithExpiry().extractAuthentication(map);

    assertEquals(expected, studySecurity.authorize(authentication, TEST_STUDY));
  }
}
