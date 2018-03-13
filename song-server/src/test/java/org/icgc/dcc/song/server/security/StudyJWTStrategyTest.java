package org.icgc.dcc.song.server.security;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.server.jwt.JWTUser;
import org.junit.Before;
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

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class})
@ActiveProfiles({"dev", "jwt"})
public class StudyJWTStrategyTest {

    @Autowired
    StudyJWTStrategy studyJWTStrategy;

    private String clientId = "client";
    private String studyId = "study001";

    private JWTUser unapprovedUser;
    private JWTUser approvedWithAccessUser;
    private JWTUser approvedWithoutAccessUser;

    @Before
    public void beforeTests() {
        val userBuilder = JWTUser
                .builder()
                .name("Demo.User@example.com")
                .firstName("Demo")
                .lastName("User")
                .email("Demo.User@example.com")
                .createdAt("2017-11-22 03:10:55")
                .lastLogin("2017-12-08 07:43:02")
                .preferredLanguage(null)
                .status("Approved")
                .roles(Arrays.asList("USER"));

        unapprovedUser = userBuilder.build();
        approvedWithAccessUser = userBuilder.build();
        approvedWithoutAccessUser = userBuilder.build();

        unapprovedUser.setStatus("Not Approved");
        approvedWithAccessUser.setRoles(Arrays.asList("song.STUDY001.upload"));
        approvedWithoutAccessUser.setRoles(Arrays.asList("song.STUDY002.upload"));
    }

    @Test
    public void testGoodOAuth2Authentication() throws Exception {
        val auth = getJWTOAuth2Authentication(approvedWithAccessUser);
        val isVerified = studyJWTStrategy.authorize(auth, studyId);
        assertThat(isVerified).isTrue();
    }

    @Test
    public void testBadOAuth2Authentication() throws Exception {
        val auth = getJWTOAuth2Authentication(approvedWithoutAccessUser);
        val isVerified = studyJWTStrategy.authorize(auth, studyId);
        assertThat(isVerified).isFalse();
    }

    private OAuth2Authentication getJWTOAuth2Authentication(JWTUser jwtUser) {
        OAuth2Request oauth2Request = new OAuth2Request(null, clientId, null, true, null, null, null, null, null);
        Authentication userauth = getAuthentication(jwtUser);
        OAuth2Authentication oauth2auth = new OAuth2Authentication(oauth2Request, userauth);
        return oauth2auth;
    }

    private Authentication getAuthentication(JWTUser jwtUser) {
        List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList();
        User user = new User("user", "", true, true, true, true, authorities);
        TestingAuthenticationToken token = new TestingAuthenticationToken(user, null, authorities);
        token.setAuthenticated(true);
        token.setDetails(jwtUser);
        return token;
    }
}
