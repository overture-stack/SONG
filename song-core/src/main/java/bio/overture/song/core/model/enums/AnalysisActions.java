package bio.overture.song.core.model.enums;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toUnmodifiableSet;

import bio.overture.song.core.utils.Streams;
import java.util.Collection;
import java.util.Set;
import lombok.NonNull;

public enum AnalysisActions {
    PUBLISH,
    UNPUBLISH,
    SUPPRESS,
    CREATE;

    private static final Set<String> SET =
            Streams.stream(values()).map(AnalysisActions::toString).collect(toUnmodifiableSet());

    public String toString() {
        return this.name();
    }

    public static AnalysisActions resolveAnalysisActions(@NonNull String analysisAction) {
        return Streams.stream(values())
                .filter(x -> x.toString().equals(analysisAction))
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        format("The analysis action '%s' cannot be resolved", analysisAction)));
    }

    public static String[] toStringArray() {
        return stream(values()).map(Enum::name).toArray(String[]::new);
    }

    public static Set<String> findIncorrectAnalysisActions(
            @NonNull Collection<String> analysisActions) {
        return analysisActions.stream().filter(x -> !SET.contains(x)).collect(toUnmodifiableSet());
    }
}
