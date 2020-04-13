package bio.overture.song.server.security;

import bio.overture.song.server.oauth.AccessTokenConverterWithExpiry;
import lombok.val;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

public class TestSystemSecurity {

  @Test
  public void testValidStudyScopeFails() {
    val m = new TreeMap<String, Object>();
    m.put("exp", 10);
    m.put("scope", Set.of("PROGRAMDATA-TEST-CA.READ"));
    test_authorize(m, false);
  }

  @Test
  public void testExpiredTokenFails() {
    val m = new TreeMap<String, Object>();
    m.put("exp", 0);
    m.put("scope", Set.of("PROGRAMDATA-TEST-CA.READ"));
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
    m.put("scope", Set.of("DCC.READ"));
    test_authorize(m, true);
  }
  @Test
  public void testExpiredValidGlobalScopeFails() {
    val m = new TreeMap<String, Object>();
    m.put("exp", 0);
    m.put("scope", Set.of("DCC.READ"));
    test_authorize(m, false);
  }

  public void test_authorize(Map<String, ?> map, boolean expected) {
    val scope = "DCC.READ";
    val systemSecurity = new SystemSecurity(scope);
    val authentication = new AccessTokenConverterWithExpiry().extractAuthentication(map);

    assertEquals(expected, systemSecurity.authorize(authentication));
  }
}
