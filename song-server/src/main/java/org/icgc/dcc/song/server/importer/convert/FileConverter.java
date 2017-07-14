package org.icgc.dcc.song.server.importer.convert;

import lombok.RequiredArgsConstructor;
import org.icgc.dcc.song.server.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.server.importer.resolvers.FileTypes;
import org.icgc.dcc.song.server.model.entity.File;

import java.util.List;
import java.util.Set;

import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.icgc.dcc.song.server.importer.convert.Converters.NA;
import static org.icgc.dcc.song.server.importer.convert.StudyConverter.getStudyId;

@RequiredArgsConstructor
public class FileConverter {

  public Set<File> convertFiles(List<PortalFileMetadata> portalFileMetadatas){
    return portalFileMetadatas.stream()
        .map(FileConverter::convertToFile)
        .collect(toImmutableSet());
  }

  private static File convertToFile(PortalFileMetadata portalFileMetadata){
    return File.create(
        getFileId(portalFileMetadata),
        getFileName(portalFileMetadata),
        getStudyId(portalFileMetadata),
        getFileSize(portalFileMetadata),
        getFileType(portalFileMetadata),
        getFileMd5sum(portalFileMetadata),
        getFileInfo()
    );
  }

  public static String getFileId(PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getObjectId();
  }

  public static String getFileName(PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getFileName();
  }

  public static long getFileSize(PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getFileSize();
  }

  public static String getFileType(PortalFileMetadata portalFileMetadata){
    return FileTypes.resolve(portalFileMetadata).getFileTypeName();
  }

  public static String getFileMd5sum(PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getFileMd5sum();
  }

  public static String getFileInfo(){
    return NA;
  }

  public static FileConverter createFileConverter() {
    return new FileConverter();
  }

}
