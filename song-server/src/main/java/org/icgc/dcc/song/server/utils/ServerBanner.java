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
package org.icgc.dcc.song.server.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.core.utils.VersionUtils;
import org.icgc.dcc.song.server.config.ValidationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;
import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Strings.padEnd;
import static com.google.common.base.Strings.repeat;
import static org.icgc.dcc.common.core.util.Joiners.WHITESPACE;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ServerBanner {

  /**
   * Dependencies.
   */
  @NonNull
  private final StandardEnvironment env;

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @PostConstruct
  public void log() {
    log.info("{}", line());
    log.info("Command:  {}", formatArguments());
    log.info("Version:  {}", getVersion());
    log.info("Built:    {}", getBuildTimestamp());
    log.info("SCM:");
    log(VersionUtils.getScmInfo());
    log.info("Profiles: {}", Arrays.toString(env.getActiveProfiles()));
    log(env);
    log.info("{}\n\n", line());
  }

  private static void log(Map<String, ?> values) {
    for (val entry : convert(values).entrySet()) {
      val key = entry.getKey();
      val text = entry.getValue() == null ? null : entry.getValue().toString();
      val value = firstNonNull(text, "").toString().replaceAll("\n", " ");

      log.info("         {}: {}", padEnd(key, 24, ' '), value);
    }
  }

  private static void log(StandardEnvironment env) {
    log.info("{}:", env);
    for (val source : env.getPropertySources()) {
      if (source instanceof SystemEnvironmentPropertySource){
        // Skip because this will cause issues with terminal display or is useless
        continue;
      }

      log.info("         {}:", source.getName());
      if (source instanceof EnumerablePropertySource) {
        val enumerable = (EnumerablePropertySource<?>) source;
        for (val propertyName : Sets.newTreeSet(ImmutableSet.copyOf(enumerable.getPropertyNames()))) {
          log.info("            - {}: {}", propertyName, enumerable.getProperty(propertyName));
        }
      }
    }
  }

  private static Map<String, Object> convert(Object values) {
    return MAPPER.configure(FAIL_ON_EMPTY_BEANS, false).convertValue(values,
        new TypeReference<Map<String, Object>>() {});
  }

  private static String line() {
    return repeat("-", 100);
  }

  private String formatArguments() {
    return "java " + WHITESPACE.join(getJavaArguments()) + " -jar " + getJarName() + " ...";
  }

  private List<String> getJavaArguments() {
    return ManagementFactory.getRuntimeMXBean().getInputArguments();
  }

  private String getJarName() {
    return new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
  }

  private static String getVersion() {
    return firstNonNull(getPackage().getImplementationVersion(), "[unknown version]");
  }

  private static String getBuildTimestamp() {
    return firstNonNull(getPackage().getSpecificationVersion(), "[unknown build timestamp]");
  }

  private static Package getPackage() {
    return ValidationConfig.class.getPackage();
  }

}

