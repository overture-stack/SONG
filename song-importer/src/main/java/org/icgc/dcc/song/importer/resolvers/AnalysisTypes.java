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

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static org.icgc.dcc.song.importer.resolvers.FileTypes.BAM;
import static org.icgc.dcc.song.importer.resolvers.FileTypes.VCF;

@RequiredArgsConstructor
public enum AnalysisTypes {

  SEQUENCING_READ("sequencingRead", BAM ),
  VARIANT_CALL("variantCall", VCF);

  private static Map<FileTypes, AnalysisTypes> map = Maps.newEnumMap(FileTypes.class);
  static {
    for (val at : values()){
      map.put(at.getFileType(), at);
    }
  }

  @Getter private final String analysisTypeName;
  @Getter private final FileTypes fileType;

  public static AnalysisTypes resolve(String analysisTypeName){
    for(val type : values()){
      if (type.getAnalysisTypeName().equals(analysisTypeName)){
        return type;
      }
    }
    throw new IllegalStateException(format("Could not resolve analysisTypeName [%s]",analysisTypeName));
  }

  public static AnalysisTypes resolve(FileTypes fileType){
    checkArgument(map.containsKey(fileType),
        "There is no AnalysisType for the fileType [%s]",
        fileType.name());
    return map.get(fileType);
  }


}

