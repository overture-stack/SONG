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

package org.icgc.dcc.song.server.config;

import lombok.NoArgsConstructor;
import org.icgc.dcc.song.server.service.ScoreService;
import org.icgc.dcc.song.server.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

import static org.icgc.dcc.song.server.service.ScoreService.createScoreService;

@NoArgsConstructor
@Configuration
public class ExistenceConfig {

  @Autowired
  private RetryTemplate retryTemplate;

  @Autowired
  private ValidationService validationService;

  @Value("${dcc-storage.url}")
  private String storageUrl;

  @Bean
  public ScoreService scoreService(){
    return createScoreService(new RestTemplate(),retryTemplate,storageUrl, validationService);
  }

}
