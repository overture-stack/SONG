package org.icgc.dcc.song.importer.convert;

import com.google.common.collect.ImmutableSet;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.icgc.dcc.song.importer.model.DccMetadata;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.importer.resolvers.FileTypes;
import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.model.enums.AccessTypes;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.icgc.dcc.song.importer.convert.AnalysisConverter.getAnalysisId;
import static org.icgc.dcc.song.importer.convert.Converters.NA;
import static org.icgc.dcc.song.importer.convert.StudyConverter.getStudyId;
import static org.icgc.dcc.song.importer.parser.FieldNames.INDEX_FILE_ID;
import static org.icgc.dcc.song.importer.parser.FieldNames.INDEX_FILE_MD5SUM;
import static org.icgc.dcc.song.importer.parser.FieldNames.INDEX_FILE_NAME;
import static org.icgc.dcc.song.importer.parser.FieldNames.INDEX_FILE_OBJECT_ID;
import static org.icgc.dcc.song.importer.parser.FieldNames.INDEX_FILE_SIZE;
import static org.icgc.dcc.song.importer.parser.FieldNames.INDEX_FILE_TYPE;
import static org.icgc.dcc.song.server.model.enums.AccessTypes.resolveAccessType;

@RequiredArgsConstructor
public class FileConverter {

  private static final long NA_LONG = -1;

  public Set<File> convertFiles(@NonNull List<PortalFileMetadata> portalFileMetadatas){
    return portalFileMetadatas.stream()
        .map(FileConverter::convertToFiles)
        .flatMap(Collection::stream)
        .collect(toImmutableSet());
  }

  public static File convertToFile(DccMetadata dccMetadata, java.io.File file, PortalFileMetadata portalFileMetadata){
    return File.create(
        getFileId(dccMetadata),
        getAnalysisId(portalFileMetadata),
        getFileName(dccMetadata),
        getStudyId(portalFileMetadata),
        getFileSize(file),
        getFileType(dccMetadata),
        getFileMd5sum(file),
        getFileAccess(dccMetadata)
    );

  }

  private static Set<File> convertToFiles(PortalFileMetadata portalFileMetadata){
    val files = ImmutableSet.<File>builder();
    val mainFile = File.create(
        getFileId(portalFileMetadata),
        getAnalysisId(portalFileMetadata),
        getFileName(portalFileMetadata),
        getStudyId(portalFileMetadata),
        getFileSize(portalFileMetadata),
        getFileType(portalFileMetadata),
        getFileMd5sum(portalFileMetadata),
        getFileAccess(portalFileMetadata)
    );
    files.add(mainFile);

    if (portalFileMetadata.isIndexFileComplete()){
      val indexFile = File.create(
          getIndexFileObjectId(portalFileMetadata),
          getIndexAnalysisId(portalFileMetadata),
          getIndexFileName(portalFileMetadata),
          getIndexStudyId(portalFileMetadata),
          getIndexFileSize(portalFileMetadata),
          getIndexFileType(portalFileMetadata),
          getIndexFileMd5sum(portalFileMetadata),
          getIndexFileAccess(portalFileMetadata)
      );
      files.add(indexFile);
    }
    return files.build();
  }

  public static AccessTypes getIndexFileAccess(@NonNull PortalFileMetadata portalFileMetadata){
    return resolveAccessType(portalFileMetadata.getAccess());
  }

  public static AccessTypes getFileAccess(@NonNull PortalFileMetadata portalFileMetadata){
    return resolveAccessType(portalFileMetadata.getAccess());
  }

  public static AccessTypes getFileAccess(@NonNull DccMetadata dccMetadata){
    return dccMetadata.getAccessType();
  }

  public static String getFileId(@NonNull DccMetadata dccMetadata){
    return dccMetadata.getId();
  }

  public static String getFileId(@NonNull PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getObjectId();
  }

  public static String getFileName(@NonNull PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getFileName();
  }

  public static String getFileName(@NonNull DccMetadata dccMetadata){
    return dccMetadata.getFileName();
  }

  public static long getFileSize(@NonNull PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getFileSize();
  }

  public static long getFileSize(@NonNull java.io.File file){
    return NA_LONG;
  }

  public static String getFileType(@NonNull PortalFileMetadata portalFileMetadata){
    return getFileTypes(portalFileMetadata).getFileTypeName();
  }

  public static String getFileType(@NonNull DccMetadata dccMetadata){
    return getFileTypes(dccMetadata).getFileTypeName();
  }

  public static FileTypes getFileTypes(@NonNull PortalFileMetadata portalFileMetadata){
    return FileTypes.resolve(portalFileMetadata);
  }

  public static FileTypes getFileTypes(@NonNull DccMetadata dccMetadata){
    return FileTypes.resolve(dccMetadata);
  }

  public static String getFileMd5sum(@NonNull PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getFileMd5sum();
  }

  public static String getFileMd5sum(@NonNull java.io.File file){
    return NA;
  }

  public static String getFileInfo(){
    return NA;
  }

  private static <T> T getIndexField(@NonNull String fieldName, @NonNull  Optional<T> optionalField){
    return optionalField.orElseThrow(() -> new IllegalStateException(format("The field '%s' is null", fieldName)));
  }

  public static String getIndexAnalysisId(@NonNull PortalFileMetadata portalFileMetadata){
    return getAnalysisId(portalFileMetadata);
  }

  public static String getIndexStudyId(@NonNull PortalFileMetadata portalFileMetadata){
    return getStudyId(portalFileMetadata);
  }

  public static String getIndexFileId(@NonNull PortalFileMetadata portalFileMetadata){
    return getIndexField(INDEX_FILE_ID, portalFileMetadata.getIndexFileId());
  }

  public static String getIndexFileObjectId(@NonNull PortalFileMetadata portalFileMetadata){
    return getIndexField(INDEX_FILE_OBJECT_ID, portalFileMetadata.getIndexFileObjectId());
  }

  public static String getIndexFileName(@NonNull PortalFileMetadata portalFileMetadata){
    return getIndexField(INDEX_FILE_NAME, portalFileMetadata.getIndexFileFileName());
  }

  public static long getIndexFileSize(@NonNull PortalFileMetadata portalFileMetadata){
    return getIndexField(INDEX_FILE_SIZE, portalFileMetadata.getIndexFileFileSize());
  }

  public static String getIndexFileType(@NonNull PortalFileMetadata portalFileMetadata){
    return getIndexField(INDEX_FILE_TYPE, portalFileMetadata.getIndexFileFileFormat());
  }

  public static FileTypes getIndexFileTypes(@NonNull PortalFileMetadata portalFileMetadata){
    return FileTypes.resolve(getIndexFileType(portalFileMetadata));
  }

  public static String getIndexFileMd5sum(@NonNull PortalFileMetadata portalFileMetadata){
    return getIndexField(INDEX_FILE_MD5SUM, portalFileMetadata.getIndexFileFileMd5sum());
  }

  public static String getIndexFileInfo(){
    return NA;
  }

  public static FileConverter createFileConverter() {
    return new FileConverter();
  }

}
