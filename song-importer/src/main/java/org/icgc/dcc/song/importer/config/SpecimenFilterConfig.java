package org.icgc.dcc.song.importer.config;

import lombok.Getter;
import lombok.val;
import org.icgc.dcc.song.importer.filters.FileFilter;
import org.icgc.dcc.song.importer.filters.Filter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.icgc.dcc.song.importer.filters.FileFilter.createFileFilter;
import static org.icgc.dcc.song.importer.filters.IdFilter.createIdFilter;

@Configuration
@ConfigurationProperties(prefix = "filters.specimen")
@Getter
public class SpecimenFilterConfig {

  @Value("${filters.specimen.enable}")
  private boolean enable;

  @Value("${filters.specimen.isGoodIds}")
  private boolean isGoodIds;

  private List<String> ids = newArrayList();

  @Bean
  public FileFilter specimenFileFilter(){
    val specimenIdFilter = enable ? createIdFilter(newHashSet(ids), isGoodIds) : Filter.<String>passThrough();
    return createFileFilter(specimenIdFilter);
  }

}
