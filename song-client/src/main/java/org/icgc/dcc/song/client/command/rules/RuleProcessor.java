package org.icgc.dcc.song.client.command.rules;

import com.google.common.collect.Maps;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.icgc.dcc.song.client.cli.Status;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;

@RequiredArgsConstructor
public class RuleProcessor {

  @NonNull private final Map<String, ModeRule> modeRuleMap;

  public Status check() {
    val definedModeRules = modeRuleMap.values().stream().filter(ModeRule::isModeDefined).collect(toImmutableList());
    val status = new Status();

    if (definedModeRules.size() > 1) {
      val sb = new StringBuilder();
      sb.append("The arguments from ");
      int count = 0;
      for (val modeRule : definedModeRules) {
        if (count > 0) {
          sb.append(" and ");
        }
        sb.append(format("the search mode '%s' (%s)",
            modeRule.getSearchMode(),
            modeRule.getDefinedTerms()
                .stream()
                .map(ParamTerm::getShortLongSymbol)
                .collect(joining(" , "))));
        count++;
      }
      sb.append(" cannot be used together\n");
      status.err(sb.toString());
    }
    return status;
  }

  public static RuleProcessor createRuleProcessor(ModeRule... modeRules) {
    val map = Maps.<String, ModeRule>newHashMap();
    for (val modeRule : modeRules) {
      val searchMode = modeRule.getSearchMode();
      checkArgument(!map.containsKey(searchMode),
          "the map already contains the search mode '%s'", searchMode);
      map.put(searchMode, modeRule);
    }
    return new RuleProcessor(map);
  }
}
