package org.icgc.dcc.song.client.command.rules;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;

@RequiredArgsConstructor
public class ModeRule {

  @NonNull @Getter private final String searchMode;
  @NonNull private final List<ParamTerm<?>> paramTerms;

  public List<ParamTerm<?>> getDefinedTerms() {
    return paramTerms.stream()
        .filter(ParamTerm::isDefined)
        .collect(toImmutableList());
  }

  public boolean isModeDefined() {
    return paramTerms.stream()
        .anyMatch(ParamTerm::isDefined);
  }

  public static ModeRule createModeRule(String searchMode, List<ParamTerm<?>> paramTerms) {
    return new ModeRule(searchMode, paramTerms);
  }

  public static ModeRule createModeRule(String searchMode, ParamTerm<?>... paramTerms) {
    return createModeRule(searchMode, newArrayList(paramTerms));
  }

}
