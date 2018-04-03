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

package org.icgc.dcc.song.client.command.rules;

import lombok.NonNull;
import lombok.Value;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;

@Value
public class ModeRule {

  @NonNull private final String modeName;
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
