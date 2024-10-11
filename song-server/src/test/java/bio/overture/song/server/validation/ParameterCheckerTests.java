/*
 * Copyright (c) 2019. Ontario Institute for Cancer Research
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

package bio.overture.song.server.validation;

import static bio.overture.song.core.exceptions.ServerErrors.ILLEGAL_FILTER_PARAMETER;
import static bio.overture.song.core.exceptions.ServerErrors.ILLEGAL_QUERY_PARAMETER;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.utils.ParameterChecker.createParameterChecker;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

import bio.overture.song.core.testing.SongErrorAssertions;
import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.model.legacy.LegacyDto;
import bio.overture.song.server.utils.ParameterChecker;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import lombok.val;
import org.junit.Test;

public class ParameterCheckerTests {

  private static final Class<?> REGISTERED_TYPE = LegacyDto.class;
  private ParameterChecker parameterChecker = createParameterChecker(REGISTERED_TYPE);
  private static Set<String> expectedFieldNames =
      newHashSet("id", "fileName", "access", "projectCode", "gnosId");
  private final RandomGenerator randomGenerator =
      createRandomGenerator(ParameterCheckerTests.class.getSimpleName());

  @Test
  public void testQueryChecker() {
    val fieldNames = randomGenerator.randomSublist(newArrayList(expectedFieldNames));
    parameterChecker.checkQueryParameters(REGISTERED_TYPE, newHashSet(fieldNames));

    // Test empty
    parameterChecker.checkQueryParameters(REGISTERED_TYPE, newHashSet());

    val corruptedFieldNameSet = buildCorruptedFieldNameSet(fieldNames);

    SongErrorAssertions.assertSongErrorRunnable(
        () ->
            parameterChecker.checkQueryParameters(
                REGISTERED_TYPE, newHashSet(corruptedFieldNameSet)),
        ILLEGAL_QUERY_PARAMETER);
  }

  @Test
  public void testFilterChecker() {
    val fieldNames = randomGenerator.randomSublist(newArrayList(expectedFieldNames));
    parameterChecker.checkFilterParameters(REGISTERED_TYPE, newHashSet(fieldNames));

    // Test empty
    parameterChecker.checkFilterParameters(REGISTERED_TYPE, newHashSet());

    val corruptedFieldNameSet = buildCorruptedFieldNameSet(fieldNames);

    SongErrorAssertions.assertSongErrorRunnable(
        () ->
            parameterChecker.checkFilterParameters(
                REGISTERED_TYPE, newHashSet(corruptedFieldNameSet)),
        ILLEGAL_FILTER_PARAMETER);
  }

  private Set<String> buildCorruptedFieldNameSet(List<String> fieldNames) {
    val randomIndex = randomGenerator.generateRandomIntRange(0, fieldNames.size());
    val corruptedFieldNameSet = Sets.<String>newHashSet();
    for (int i = 0; i < fieldNames.size(); i++) {
      if (i == randomIndex) {
        corruptedFieldNameSet.add(fieldNames.get(i) + "_nonsense");
      } else {
        corruptedFieldNameSet.add(fieldNames.get(i));
      }
    }
    return corruptedFieldNameSet;
  }
}
