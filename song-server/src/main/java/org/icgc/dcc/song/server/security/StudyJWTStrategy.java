package org.icgc.dcc.song.server.security;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.server.jwt.JWTUser;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("jwt")
public class StudyJWTStrategy implements StudyStrategyInterface {

    public boolean authorize(@NonNull Authentication authentication, @NonNull final String studyId) {
        log.info("Checking authorization with study id {}", studyId);

        val details = (OAuth2AuthenticationDetails) authentication.getDetails();
        val user = (JWTUser) details.getDecodedDetails();

        final String REQUIRED_ROLE = "song.STUDY001.upload";
        return user.getRoles().contains(REQUIRED_ROLE);
    }

}
