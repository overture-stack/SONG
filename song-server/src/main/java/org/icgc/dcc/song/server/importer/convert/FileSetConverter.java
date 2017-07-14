package org.icgc.dcc.song.server.importer.convert;

import lombok.NonNull;
import org.icgc.dcc.song.server.importer.model.FileSet;
import org.icgc.dcc.song.server.importer.model.PortalFileMetadata;

import java.util.List;
import java.util.Set;

import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.icgc.dcc.song.server.importer.convert.AnalysisConverter.getAnalysisId;
import static org.icgc.dcc.song.server.importer.convert.FileConverter.getFileId;
import static org.icgc.dcc.song.server.importer.model.FileSet.createFileSet;

public class FileSetConverter {

  public Set<FileSet> convertFileSets(@NonNull List<PortalFileMetadata> portalFileMetadataList){
    return portalFileMetadataList.stream()
        .map(FileSetConverter::convertToFileSet)
        .collect(toImmutableSet());
  }

  public static FileSet convertToFileSet(@NonNull PortalFileMetadata portalFileMetadata){
    return createFileSet(
        getAnalysisId(portalFileMetadata),
        getFileId(portalFileMetadata)
    );
  }

  public static FileSetConverter createFileSetConverter() {
    return new FileSetConverter();
  }

}
