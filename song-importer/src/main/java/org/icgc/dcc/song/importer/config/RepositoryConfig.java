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

import javax.sql.DataSource;

import org.icgc.dcc.song.server.repository.*;
import org.skife.jdbi.v2.DBI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Lazy
@Configuration
public class RepositoryConfig {

  @Autowired
  private DataSource dataSource;

  @Bean
  public DBI dbi() {
    return new DBI(dataSource);
  }

  @Bean
  public StudyRepository studyRepository(DBI dbi) {
    return dbi.open(StudyRepository.class);
  }

  @Bean
  public DonorRepository donorRepository(DBI dbi) {
    return dbi.open(DonorRepository.class);
  }

  @Bean
  public SpecimenRepository SpecimenRepository(DBI dbi) {
    return dbi.open(SpecimenRepository.class);
  }

  @Bean
  public SampleRepository SampleRepository(DBI dbi) {
    return dbi.open(SampleRepository.class);
  }

  @Bean
  public FileRepository FileRepository(DBI dbi) {
    return dbi.open(FileRepository.class);
  }

  @Bean
  public UploadRepository statusRepository(DBI dbi) {
    return dbi.open(UploadRepository.class);
  }

  @Bean
  public AnalysisRepository AnalysisRepository(DBI dbi) {
    return dbi.open(AnalysisRepository.class);
  }

 @Bean
  public InfoRepository InfoRepository(DBI dbi) {
    return dbi.open(InfoRepository.class);
  }

}
