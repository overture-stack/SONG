package org.icgc.dcc.song.importer.convert;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.importer.resolvers.FileTypes;
import org.icgc.dcc.song.server.model.entity.File;

import java.util.List;
import java.util.Set;

import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.icgc.dcc.song.importer.convert.Converters.NA;

@RequiredArgsConstructor
public class FileConverter {

  public Set<File> convertFiles(@NonNull List<PortalFileMetadata> portalFileMetadatas){
    return portalFileMetadatas.stream()
        .map(FileConverter::convertToFile)
        .collect(toImmutableSet());
  }

  private static File convertToFile(PortalFileMetadata portalFileMetadata){
    return File.create(
        getFileId(portalFileMetadata),
        getFileName(portalFileMetadata),
        StudyConverter.getStudyId(portalFileMetadata),
        getFileSize(portalFileMetadata),
        getFileType(portalFileMetadata),
        getFileMd5sum(portalFileMetadata),
        getFileInfo()
    );
  }

  public static String getFileId(@NonNull PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getObjectId();
  }

  public static String getFileName(@NonNull PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getFileName();
  }

  public static long getFileSize(@NonNull PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getFileSize();
  }

  public static String getFileType(@NonNull PortalFileMetadata portalFileMetadata){
    return getFileTypes(portalFileMetadata).getFileTypeName();
  }

  public static FileTypes getFileTypes(@NonNull PortalFileMetadata portalFileMetadata){
    return FileTypes.resolve(portalFileMetadata);

  }

  public static String getFileMd5sum(@NonNull PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getFileMd5sum();
  }

  public static String getFileInfo(){
    return NA;
  }

  public static FileConverter createFileConverter() {
    return new FileConverter();
  }

}
