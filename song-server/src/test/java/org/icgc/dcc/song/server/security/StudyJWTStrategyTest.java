package org.icgc.dcc.song.server.security;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class})
@ActiveProfiles("dev")
public class StudyJWTStrategyTest {

    @Autowired
    StudyJWTStrategy studyJWTStrategy;

    String clientId = "client";
    String studyId = "study001";

    @Test
    public void testGoodOAuth2Authentication() throws Exception {
        val auth = getOAuth2Authentication("song.STUDY001.upload");
        val isVerified = studyJWTStrategy.authorize(auth, studyId);
        assertThat(isVerified).isTrue();
    }

    @Test
    public void testBadOAuth2Authentication() throws Exception {
        val auth = getOAuth2Authentication("song.STUDY002.upload");
        val isVerified = studyJWTStrategy.authorize(auth, studyId);
        assertThat(isVerified).isFalse();
    }

    private OAuth2Authentication getOAuth2Authentication(String studyId) {
        Set<String> grantedScopes = new HashSet<>();
        grantedScopes.add(studyId);
        OAuth2Request oauth2Request = new OAuth2Request(null, clientId, null, true, grantedScopes, null, null, null, null);
        Authentication userauth = getAuthentication();
        OAuth2Authentication oauth2auth = new OAuth2Authentication(oauth2Request, userauth);
        return oauth2auth;
    }

    private Authentication getAuthentication() {
        List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList();
        User user = new User("user", "", true, true, true, true, authorities);
        TestingAuthenticationToken token = new TestingAuthenticationToken(user, null, authorities);
        token.setAuthenticated(true);
        return token;
    }
}
