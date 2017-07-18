package org.icgc.dcc.song.importer.filters;

import lombok.RequiredArgsConstructor;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;

@RequiredArgsConstructor
public class FileFilter implements Filter<PortalFileMetadata> {

  private final Filter<String> specimenIdFilter;

  @Override public boolean isPass(PortalFileMetadata portalFileMetadata) {
    return specimenIdFilter.filterStream(portalFileMetadata.getSpecimenIds()).count() > 0;
  }

  public static FileFilter createFileFilter(Filter<String> specimenIdFilter) {
    return new FileFilter(specimenIdFilter);
  }

}
