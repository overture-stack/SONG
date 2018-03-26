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

package org.icgc.dcc.song.importer.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.importer.download.DownloadIterator;
import org.icgc.dcc.song.importer.model.DccMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import static org.icgc.dcc.song.importer.convert.DccMetadataUrlConverter.createDccMetadataUrlConverter;
import static org.icgc.dcc.song.importer.download.DownloadIterator.createDownloadIterator;
import static org.icgc.dcc.song.importer.download.urlgenerator.impl.DccMetadataUrlGenerator.createDccMetadataUrlGenerator;

@Slf4j
@Configuration
@Lazy
@Getter
public class DccMetadataConfig {

  private static final int DCC_METADATA_MAX_FETCH_SIZE = 2000;
  private static final int DCC_METADATA_INITIAL_FROM = 0;

  @Value("${dcc-metadata.url}")
  private String url;

  @Value("${dcc-metadata.fetchSize}")
  private int fetchSize;

  @Bean
  public DownloadIterator<DccMetadata> dccMetadataDownloadIterator(){
    log.info("Building DccMetadata DownloadIterator with url: {} and fetchSize: {}", url, fetchSize);
    val urlGenerator = createDccMetadataUrlGenerator(url);
    val urlConverter = createDccMetadataUrlConverter();
    return createDownloadIterator(urlConverter,urlGenerator, fetchSize,
        DCC_METADATA_MAX_FETCH_SIZE, DCC_METADATA_INITIAL_FROM);
  }

}
