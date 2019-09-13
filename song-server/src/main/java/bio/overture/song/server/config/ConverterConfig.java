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

package bio.overture.song.server.config;

import static java.util.regex.Pattern.compile;

import java.util.regex.Pattern;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.MapperConfig;
import org.mapstruct.MappingInheritanceStrategy;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

@MapperConfig(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
    mappingInheritanceStrategy = MappingInheritanceStrategy.AUTO_INHERIT_ALL_FROM_CONFIG)
public class ConverterConfig {

  private static final String ANALYSIS_TYPE_NAME_REGEX = "[a-zA-Z0-9\\._-]+";

  public static final String ANALYSIS_TYPE_ID_STRING_FORMAT = "%s:%s";
  public static final Pattern ANALYSIS_TYPE_NAME_PATTERN =
      compile("^" + ANALYSIS_TYPE_NAME_REGEX + "$");
  public static final Pattern ANALYSIS_TYPE_ID_PATTERN =
      compile("^(" + ANALYSIS_TYPE_NAME_REGEX + "):(\\d+)$");
}
