/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package bio.overture.song.client.command.rules;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;

import bio.overture.song.client.cli.Status;
import com.google.common.collect.Maps;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class RuleProcessor {

  @NonNull private final Map<String, ModeRule> modeRuleMap;

  public Status check() {
    val definedModeRules =
        modeRuleMap.values().stream().filter(ModeRule::isModeDefined).collect(toImmutableList());
    val status = new Status();

    if (definedModeRules.size() > 1) {
      val sb = new StringBuilder();
      sb.append("The arguments from ");
      int count = 0;
      for (val modeRule : definedModeRules) {
        if (count > 0) {
          sb.append(" and ");
        }
        sb.append(
            format(
                "the mode '%s' (%s)",
                modeRule.getModeName(),
                modeRule.getDefinedTerms().stream()
                    .map(ParamTerm::getShortLongSymbol)
                    .collect(joining(" , "))));
        count++;
      }
      sb.append(" cannot be used together\n");
      status.err(sb.toString());
    } else if (definedModeRules.size() == 0) {
      val sb = new StringBuilder();
      sb.append("No modes defined. Please define one of:\n");
      for (val modeRule : modeRuleMap.values()) {
        val modeName = modeRule.getModeName();
        val orOfSwitches =
            modeRule.getParamTerms().stream()
                .map(ParamTerm::getShortLongSymbol)
                .collect(joining(" | "));
        sb.append(format("\t[%s]: (%s)\n", modeName, orOfSwitches));
      }
      status.err(sb.toString());
    }

    return status;
  }

  public static RuleProcessor createRuleProcessor(ModeRule... modeRules) {
    val map = Maps.<String, ModeRule>newHashMap();
    for (val modeRule : modeRules) {
      val searchMode = modeRule.getModeName();
      checkArgument(
          !map.containsKey(searchMode),
          "the map already contains the search mode '%s'",
          searchMode);
      map.put(searchMode, modeRule);
    }
    return new RuleProcessor(map);
  }
}
