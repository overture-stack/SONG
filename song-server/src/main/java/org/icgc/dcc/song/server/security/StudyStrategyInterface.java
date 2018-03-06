package org.icgc.dcc.song.server.security;

import lombok.NonNull;
import org.springframework.security.core.Authentication;

public interface StudyStrategyInterface {
    public boolean authorize(@NonNull Authentication authentication, @NonNull final String studyId);
}
