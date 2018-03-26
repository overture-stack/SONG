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
package org.icgc.dcc.song.core.utils;

import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Properties;

import static com.google.common.base.Objects.firstNonNull;
import static com.google.common.collect.Maps.fromProperties;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class VersionUtils {

  private static final String LOCAL_VERSION = "?";

  private static final Map<String, String> SCM_INFO = loadScmInfo();

  public static Map<String, String> getScmInfo() {
    return SCM_INFO;
  }

  private static final String VERSION = firstNonNull(
      VersionUtils.class.getPackage().getImplementationVersion(),
      LOCAL_VERSION);

  public static String getVersion() {
    return VERSION;
  }

  public static String getApiVersion() {
    return "v" + VERSION.split("\\.")[0];
  }

  public static String getCommitId() {
    return firstNonNull(SCM_INFO.get("git.commit.id.abbrev"), "unknown");
  }

  public static String getCommitMessageShort() {
    return firstNonNull(SCM_INFO.get("git.commit.message.short"), "unknown");
  }

  private static Map<String, String> loadScmInfo() {
    Properties properties = new Properties();
    try {
      properties.load(VersionUtils.class.getClassLoader().getResourceAsStream("git.properties"));
    } catch (Exception e) {
      // Local build
    }

    return fromProperties(properties);
  }

}
