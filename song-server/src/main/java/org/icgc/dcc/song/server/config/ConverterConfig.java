package org.icgc.dcc.song.server.config;

import org.icgc.dcc.song.server.converter.LegacyEntityConverter;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConverterConfig {

  @Bean
  public LegacyEntityConverter legacyEntityConverter(){
    return Mappers.getMapper(LegacyEntityConverter.class);
  }

}
