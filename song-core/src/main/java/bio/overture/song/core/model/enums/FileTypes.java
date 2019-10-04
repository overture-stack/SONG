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

package bio.overture.song.core.model.enums;

import static bio.overture.song.core.utils.Streams.stream;
import static java.lang.String.format;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FileTypes {
  FASTA("fasta"),
  FAI("fai"),
  FASTQ("fastq"),
  BAM("bam"),
  BAI("bai"),
  VCF("vcf"),
  TBI("tbi"),
  IDX("idx"),
  XML("xml"),
  TGZ("tgz");

  @Getter private final String extension;

  public static FileTypes resolveFileType(@NonNull String fileType) {
    return stream(values())
        .filter(x -> x.toString().equals(fileType))
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalStateException(
                    format("The file type '%s' cannot be resolved", fileType)));
  }

  @Override
  public String toString() {
    return this.name();
  }
}
