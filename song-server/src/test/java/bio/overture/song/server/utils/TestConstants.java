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

package bio.overture.song.server.utils;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toUnmodifiableList;
import static lombok.AccessLevel.PRIVATE;

import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class TestConstants {

  public static final String DEFAULT_STUDY_ID = "ABC123";
  public static final String DEFAULT_ANALYSIS_ID = "AN1";
  public static final String DEFAULT_FILE_ID = "FI1";

  @Getter
  @RequiredArgsConstructor
  public enum TumourNormalDesignations implements Constant {
    NORMAL("Normal"),
    TUMOUR("Tumour");
    @NonNull private final String text;
  }

  @Getter
  @RequiredArgsConstructor
  public enum Genders implements Constant {
    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other");

    @NonNull private final String text;
  }

  public interface Constant {
    String getText();
  }

  public static final List<String> TUMOUR_NORMAL_DESIGNATION =
      getStringList(TumourNormalDesignations.values());

  public static final Collection<String> GENDER = getStringList(Genders.values());

  private static <T extends Constant> List<String> getStringList(T[] constants) {
    return stream(constants).map(Constant::getText).collect(toUnmodifiableList());
  }
}
