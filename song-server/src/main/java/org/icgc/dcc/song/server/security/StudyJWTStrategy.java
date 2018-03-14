package org.icgc.dcc.song.server.security;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.server.jwt.JWTUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.icgc.dcc.common.core.util.Joiners.DOT;

@Slf4j
@Component
@Profile("jwt")
public class StudyJWTStrategy implements StudyStrategyInterface {

    @Value("${auth.server.prefix}")
    protected String scopePrefix;

    @Value("${auth.server.suffix}")
    protected String scopeSuffix;

    public boolean authorize(@NonNull Authentication authentication, @NonNull final String studyId) {
        log.info("Checking authorization with study id {}", studyId);

        val details = (OAuth2AuthenticationDetails) authentication.getDetails();
        val user = (JWTUser) details.getDecodedDetails();

        return verify(user, studyId);
    }

    boolean verify(JWTUser user, String studyId) {
        final val roles = user.getRoles();
        val check = roles.stream().filter(s -> isGranted(s, studyId)).collect(toList());
        return !check.isEmpty();
    }

    private boolean isGranted(String tokenScope, String studyId) {
        log.info("Checking JWT's scope '{}', server's scopePrefix='{}', studyId '{}', scopeSuffix='{}'",
                tokenScope, scopePrefix, studyId, scopeSuffix);
        return getSystemScope().equals(tokenScope) || getEndUserScope(studyId).equals(tokenScope); //short-circuit
    }

    private String getEndUserScope(String studyId) {
        return DOT.join(scopePrefix, studyId.toUpperCase(), scopeSuffix);
    }

    private String getSystemScope() {
        return DOT.join(scopePrefix, scopeSuffix);
    }
}
