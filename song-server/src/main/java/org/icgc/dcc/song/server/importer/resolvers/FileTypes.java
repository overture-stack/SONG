package org.icgc.dcc.song.server.importer.resolvers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.icgc.dcc.song.server.importer.model.PortalFileMetadata;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

@RequiredArgsConstructor
public enum FileTypes {
  FASTA("FASTA"),
  FAI("FAI"),
  FASTQ("FASTQ"),
  BAM("BAM"),
  BAI("BAI"),
  VCF("VCF"),
  TBI("TBI"),
  IDX("IDX");

  @Getter private final String fileTypeName;

  public static FileTypes resolve(PortalFileMetadata portalFileMetadata){
    val fileFormat = portalFileMetadata.getFileFormat();
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
