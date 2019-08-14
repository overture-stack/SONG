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

package bio.overture.song.core.errors;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;

import static java.lang.Thread.currentThread;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Streams.stream;
import static org.junit.Assert.assertTrue;
import static bio.overture.song.core.utils.Debug.getCallingStackTrace;

@Slf4j
public class DebugTest {

  @Test
  public void testCallingStackTrace(){
    val expectedSt = stream(currentThread().getStackTrace()).skip(2).collect(toImmutableList());
    val actualSt = getCallingStackTrace();
    assertTrue(actualSt.containsAll(expectedSt));
  }


}
