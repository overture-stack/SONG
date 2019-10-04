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

package bio.overture.song.core.utils;

import static bio.overture.song.core.utils.Joiners.NEWLINE;
import static bio.overture.song.core.utils.Streams.stream;
import static java.lang.Thread.currentThread;
import static java.util.stream.Collectors.toUnmodifiableList;
import static lombok.AccessLevel.PRIVATE;

import java.util.List;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import lombok.val;

@NoArgsConstructor(access = PRIVATE)
public class Debug {

  /** Gets the stacktrace List of the calling function */
  public static List<StackTraceElement> getCallingStackTrace() {
    return stream(currentThread().getStackTrace()).skip(2).collect(toUnmodifiableList());
  }

  public static Stream<StackTraceElement> streamCallingStackTrace() {
    return stream(currentThread().getStackTrace()).skip(2);
  }

  public static void sleepMs(long timeMs) {
    try {
      Thread.sleep(timeMs);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public static String generateHeader(String title, int lengthOfHeader, String symbol) {
    int t = title.length();
    int n = Math.max(t + 2, lengthOfHeader);
    int c = symbol.length();
    int leftTerm = c * (n - 2);
    String adjustedTitle = title;
    boolean isLeftTermOdd = leftTerm % 2 == 1;
    boolean isRightTermOdd = t % 2 == 1;
    int adjusted_t = t;

    // Odd - Odd = Even, so if title is even, add an extra whitespace so its odd
    if (isLeftTermOdd || isRightTermOdd) {
      adjusted_t += 1;
      adjustedTitle += " ";
    }
    int p = (leftTerm - adjusted_t) / 2; // numerator is even

    String line = generateChars(symbol, n);
    String pWhiteSpace = generateChars(" ", p);
    String center = symbol + pWhiteSpace + adjustedTitle + pWhiteSpace + symbol;
    return NEWLINE.join(line, center, line);
  }

  public static String generateChars(String c, int num) {
    val sb = new StringBuilder(num);
    for (int i = 0; i < num; i++) {
      sb.append(c);
    }
    return sb.toString();
  }
}
