package org.icgc.dcc.song.server.security;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.server.jwt.JWTUser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class})
@ActiveProfiles({"dev", "jwt"})
public class StudyJWTStrategyTest {

    @Autowired
    StudyJWTStrategy studyJWTStrategy;

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

        // Careful with underlying references
        unapprovedUser.setStatus("Not Approved");
        approvedWithAccessUser.setRoles(Arrays.asList("USER", "song.STUDY001.upload"));
        approvedWithoutAccessUser.setRoles(Arrays.asList("USER", "song.STUDY002.upload"));
    }

    @Test
    public void testJWTUserWithAccess() throws Exception {
        val isVerified = studyJWTStrategy.verify(approvedWithAccessUser, "STUDY001");
        assertThat(isVerified).isTrue();
    }

    @Test
    public void testJWTUserWithoutAccess() throws Exception {
        val isVerified = studyJWTStrategy.verify(approvedWithoutAccessUser, "STUDY001");
        assertThat(isVerified).isFalse();
    }
}
