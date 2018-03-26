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
package org.icgc.dcc.song.importer;

import lombok.Getter;
import org.icgc.dcc.song.importer.download.DownloadIterator;
import org.icgc.dcc.song.importer.filters.Filter;
import org.icgc.dcc.song.importer.filters.impl.DataBundleFileFilter;
import org.icgc.dcc.song.importer.model.DccMetadata;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.importer.storage.SimpleDccStorageClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@Lazy
public class Config {

  @Autowired
  private SimpleDccStorageClient simpleDccStorageClient;

  @Autowired
  private DownloadIterator<DccMetadata> dccMetadataDownloadIterator;

  @Autowired
  private DownloadIterator<PortalFileMetadata> portalFileMetadataDownloadIterator;

  @Autowired
  private Filter<PortalFileMetadata> portalFileMetadataFilter;

  @Autowired
  private DataBundleFileFilter dataBundleFileFilter;

  @Getter
  @Value("${importer.updateMatchedNormalSubmitterSamples}")
  private Boolean updateMatchedNormalSubmitterSamples;

  @Getter
  @Value("${importer.disableSSL}")
  private boolean disableSSL;

  public static final String PORTAL_API = "https://dcc.icgc.org";
  public static final Path PERSISTED_DIR_PATH = Paths.get("persisted");
  public static final String DATA_CONTAINER_PERSISTENCE_FN = "dataContainer.dat";


  @Bean
  public Factory factory(){
    return new Factory(simpleDccStorageClient, dccMetadataDownloadIterator,
        portalFileMetadataDownloadIterator, portalFileMetadataFilter, dataBundleFileFilter);
  }

}
