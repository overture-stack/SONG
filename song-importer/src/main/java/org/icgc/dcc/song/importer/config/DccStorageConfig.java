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
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.icgc.dcc.song.importer.storage.SimpleDccStorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.icgc.dcc.song.importer.storage.SimpleDccStorageClient.createSimpleDccStorageClient;

@Slf4j
@Configuration
@Getter
@Setter
public class DccStorageConfig {

  @Value("${dcc-storage.token}")
  private String token;

  @Value("${dcc-storage.url}")
  private String url;

  @Value("${dcc-storage.persist}")
  private boolean persist;

  @Value("${dcc-storage.bypassMd5Check}")
  private boolean bypassMd5Check;

  @Value("${dcc-storage.outputDir}")
  private String outputDir;

  @Value("${dcc-storage.forceDownload}")
  private boolean forceDownload;

  @Bean
  public SimpleDccStorageClient simpleDccStorageClient(DccStorageConfig dccStorageConfig){
    log.info("Building SimpleDccStorageClient with url: {}, persist: {}, bypassMd5Check: {}, outputDir: {}, "
        + "forceDownload: {}", url, persist, bypassMd5Check, outputDir, forceDownload);
    return createSimpleDccStorageClient(dccStorageConfig);
  }

}
