package bio.overture.song.server.security;

import bio.overture.song.server.oauth.AccessTokenConverterWithExpiry;
import bio.overture.song.server.service.auth.KeycloakAuthorizationService;
import lombok.val;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.Assert.assertEquals;

@SpringBootTest
public class TestStudySecurity {
    String TEST_STUDY = "TEST-CA";

  @Autowired
  private KeycloakAuthorizationService keycloakAuthorizationService;

    @Test
    public void testValidStudyScopeOK() {
        val m = new TreeMap<String, Object>();
        m.put("exp", 10);
        m.put("scope", Set.of("PROGRAMDATA-TEST-CA.READ"));
        test_authorize(m, true);
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
    public void testValidGlobalScopeFails() {
        val m = new TreeMap<String, Object>();
        m.put("exp", 0);
        m.put("scope", Set.of("DCC.READ"));
        test_authorize(m, false);
    }

    public void test_authorize(Map<String, ?> map, boolean expected) {
        val prefix = "PROGRAMDATA-";
        val suffix = ".READ";
        val scope = "DCC.READ";
        val studySecurity = StudySecurity.builder()
            .studyPrefix(prefix)
            .studySuffix(suffix)
            .systemScope(scope)
            .build();
        val authentication = new AccessTokenConverterWithExpiry().extractAuthentication(map);

        assertEquals(expected, studySecurity.authorize(authentication, TEST_STUDY));
    }
}
