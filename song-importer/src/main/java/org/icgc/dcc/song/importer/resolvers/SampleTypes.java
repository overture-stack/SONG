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

package org.icgc.dcc.song.importer.resolvers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.importer.model.PortalSampleMetadata;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

@RequiredArgsConstructor
public enum SampleTypes {

  DNA("DNA", "^WXS|WGS$"),
  RNA("RNA", "^RNA-Seq|miRNA-Seq$");

  public static EnumMap<SampleTypes, Pattern> map;

  static {
    map = new EnumMap<SampleTypes, Pattern>(SampleTypes.class);
    for (val sampleTypes: values()){
      val pattern = Pattern.compile(sampleTypes.getRegex());
      map.put(sampleTypes, pattern);
    }
  }

  @Getter private final String sampleTypeName;
  @Getter private final String regex;

  public static SampleTypes resolve(PortalSampleMetadata portalSampleMetadata){
    return resolve(portalSampleMetadata.getLibraryStrategy().orElse("").trim());
  }

  public static SampleTypes resolve(PortalFileMetadata portalFileMetadata){
    return resolve(portalFileMetadata.getExperimentalStrategy());
  }

  public static SampleTypes resolve(String libraryStrategy){
    for (val sampleTypes : values()){
      val pattern = map.get(sampleTypes);
      val matcher = pattern.matcher(libraryStrategy);
      if (matcher.matches()){
        return sampleTypes;
      }
    }
    throw new IllegalStateException(format(
        "The portalSampleMetadata.libraryStrategy [%s] does not match regex for any of the "
            + "following: [%s]",
        libraryStrategy,
        Arrays.stream(values())
            .map(SampleTypes::getRegex)
            .collect(joining("] , ["))));
  }




}
