package org.icgc.dcc.song.importer.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
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
import static org.icgc.dcc.common.core.util.Joiners.COMMA;
import static org.icgc.dcc.song.importer.filters.impl.IdFilter.createIdFilter;
import static org.icgc.dcc.song.importer.filters.impl.SpecimenFileFilter.createSpecimenFileFilter;

@Slf4j
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
    log.info("Building SpecimenFileFilter {} for specimenIds: {}", enable ? "ENABLED" : "DISABLED", COMMA.join(ids));
    val specimenIdFilter = enable ? createIdFilter(newHashSet(ids), isGoodIds) : Filter.<String>passThrough();
    return createSpecimenFileFilter(specimenIdFilter);
  }

}
