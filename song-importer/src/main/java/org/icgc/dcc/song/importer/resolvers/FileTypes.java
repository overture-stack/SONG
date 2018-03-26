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
import org.icgc.dcc.song.importer.model.DccMetadata;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

@RequiredArgsConstructor
public enum FileTypes {
  BAM("BAM"),
  VCF("VCF"),
  BAI("BAI"),
  TBI("TBI"),
  XML("XML"),
  IDX("IDX");

  @Getter private final String fileTypeName;

  public static FileTypes resolve(PortalFileMetadata portalFileMetadata){
    val fileFormat = portalFileMetadata.getFileFormat();
    return resolve(fileFormat);
  }

  public static FileTypes resolve(DccMetadata dccMetadata){
    val fileFormat = dccMetadata.getFileName()
        .replaceAll(".*\\.", "")
        .toUpperCase();
    return resolve(fileFormat);
  }

  public static FileTypes resolve(String fileFormat){
    for (val fileType : values()){
      if(fileType.getFileTypeName().equals(fileFormat)){
        return fileType;
      }
    }
    throw new IllegalStateException(format(
        "The portalFileMetadata.fileFormat [%s] does not equal any of the "
            + "following: [%s]",
        fileFormat,
        stream(values())
            .map(FileTypes::getFileTypeName)
            .collect(joining(", "))));
  }




}
