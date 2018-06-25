package org.icgc.dcc.song.server.model.enums;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.icgc.dcc.common.core.util.stream.Streams;

import static java.lang.String.format;

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
 XML("xml");

 @Getter private final String extension;

 public static FileTypes resolveFileType(@NonNull String fileType){
  return Streams.stream(values())
      .filter(x -> x.toString().equals(fileType))
      .findFirst()
      .orElseThrow(() -> new IllegalStateException(format("The file type '%s' cannot be resolved", fileType)));
 }

 @Override public String toString() {
   return this.name();
 }

}
