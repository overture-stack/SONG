package org.icgc.dcc.song.importer.filters.impl;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.icgc.dcc.song.importer.filters.Filter;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DataBundleFileFilter implements Filter<PortalFileMetadata> {

  @NonNull
  @Getter
  private final DataBundleIdFilter dataBundleIdFilter;

  @Override public boolean isPass(PortalFileMetadata portalFileMetadata) {
    return dataBundleIdFilter.isPass(portalFileMetadata.getRepoDataBundleId());
  }

  public static DataBundleFileFilter createDataBundleFileFilter(DataBundleIdFilter dataBundleIdFilter) {
    return new DataBundleFileFilter(dataBundleIdFilter);
  }

}
