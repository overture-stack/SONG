package org.icgc.dcc.song.importer.config;

import lombok.Getter;
import lombok.val;
import org.icgc.dcc.song.importer.filters.Filter;
import org.icgc.dcc.song.importer.filters.impl.SpecimenFileFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.icgc.dcc.song.importer.filters.impl.IdFilter.createIdFilter;
import static org.icgc.dcc.song.importer.filters.impl.SpecimenFileFilter.createSpecimenFileFilter;

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
  public SpecimenFileFilter specimenFileFilter(){
    val specimenIdFilter = enable ? createIdFilter(newHashSet(ids), isGoodIds) : Filter.<String>passThrough();
    return createSpecimenFileFilter(specimenIdFilter);
  }

}
