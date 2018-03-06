package org.icgc.dcc.song.server.security;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!legacy")
public class StudyJWTStrategy implements StudyStrategyInterface {

    public boolean authorize(@NonNull Authentication authentication, @NonNull final String studyId) {
     return true;
    }

}
