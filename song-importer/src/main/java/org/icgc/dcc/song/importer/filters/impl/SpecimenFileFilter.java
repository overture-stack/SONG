package org.icgc.dcc.song.importer.filters.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.icgc.dcc.song.importer.filters.Filter;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;
import org.springframework.stereotype.Component;

/**
 * Filters out files that map to blacklisted specimen Ids.
 */
@RequiredArgsConstructor
@Component
public class SpecimenFileFilter implements Filter<PortalFileMetadata> {

  @NonNull private final Filter<String> specimenIdFilter;

  @Override public boolean isPass(PortalFileMetadata portalFileMetadata) {
    return specimenIdFilter.passStream(portalFileMetadata.getSpecimenIds()).count() > 0;
  }

  public static SpecimenFileFilter createSpecimenFileFilter(Filter<String> specimenIdFilter) {
    return new SpecimenFileFilter(specimenIdFilter);
  }

}
