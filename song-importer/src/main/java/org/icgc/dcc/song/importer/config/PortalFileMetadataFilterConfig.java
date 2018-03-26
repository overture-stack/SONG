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

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.importer.filters.Filter;
import org.icgc.dcc.song.importer.filters.impl.DataBundleFileFilter;
import org.icgc.dcc.song.importer.filters.impl.SpecimenFileFilter;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.readAllLines;
import static org.icgc.dcc.common.core.util.Joiners.COMMA;
import static org.icgc.dcc.song.importer.filters.Filter.cascade;
import static org.icgc.dcc.song.importer.filters.impl.DataBundleFileFilter.createDataBundleFileFilter;
import static org.icgc.dcc.song.importer.filters.impl.DataBundleIdFilter.createDataBundleIdFilter;
import static org.icgc.dcc.song.importer.filters.impl.IdFilter.createIdFilter;
import static org.icgc.dcc.song.importer.filters.impl.SpecimenFileFilter.createSpecimenFileFilter;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "filters.specimen")
@Getter
public class PortalFileMetadataFilterConfig {

  @Value("${filters.specimen.enable}")
  private boolean enable;

  @Value("${filters.specimen.isGoodIds}")
  private boolean isGoodIds;

  private List<String> ids = newArrayList();

  @Value("${filters.dataBundle.enable}")
  private boolean enableDataBundle;

  @Value("${filters.dataBundle.isGoodIds}")
  private boolean isDataBundleGoodIds;

  @Value("${filters.dataBundle.inputFilePath}")
  private String dataBundleInputFilePath;


  private static String getEnabledText(boolean enabled){
    return enabled ? "ENABLED" : "DISABLED";
  }

  @Bean
  public SpecimenFileFilter specimenFileFilter(){
    log.info("Building SpecimenFileFilter({}) for specimenIds: {}", getEnabledText(enable), COMMA.join(ids));
    val specimenIdFilter = enable ? createIdFilter(newHashSet(ids), isGoodIds) : Filter.<String>passThrough();
    return createSpecimenFileFilter(specimenIdFilter);
  }

  @Bean
  @SneakyThrows
  public DataBundleFileFilter dataBundleFileFilter(){
    List<String> dataBundleIds = null;
    if (enableDataBundle) {
      log.info("Building DataBundleFileFilter({}) for inputFile: {}", getEnabledText(enableDataBundle),
          dataBundleInputFilePath);
      val path = Paths.get(dataBundleInputFilePath);
      checkState(exists(path) && isRegularFile(path),
          "The file '%s' does not exist", path);

      dataBundleIds = readAllLines(Paths.get(dataBundleInputFilePath));
    } else {
      log.info("Skipping building of DataBundleFileFilter for inputFile: {}", getEnabledText(enableDataBundle),
          dataBundleInputFilePath);
      dataBundleIds = ImmutableList.<String>of();
    }
    val dataBundleIdFilter = createDataBundleIdFilter(enableDataBundle, newHashSet(dataBundleIds),isDataBundleGoodIds);
    return createDataBundleFileFilter(dataBundleIdFilter);
  }

  @Bean
  public Filter<PortalFileMetadata> portalFileMetadataFilter(SpecimenFileFilter specimenFileFilter,
      DataBundleFileFilter dataBundleFileFilter){
    log.info("Cascading SpecimenFileFilter({}) and DataBundleFileFilter({})",
        getEnabledText(enable), getEnabledText(enableDataBundle));
    return cascade(specimenFileFilter, dataBundleFileFilter);
  }

}
