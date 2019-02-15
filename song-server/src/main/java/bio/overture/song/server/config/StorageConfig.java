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

package bio.overture.song.server.config;

import static bio.overture.song.server.service.StorageService.createStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;
import bio.overture.song.server.service.StorageService;
import bio.overture.song.server.service.ValidationService;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Configuration
public class StorageConfig {

  @Autowired
  private RetryTemplate retryTemplate;

  @Autowired
  private ValidationService validationService;

  @Value("${score.url}")
  private String storageUrl;

  @Value("#{'Bearer '.concat('${score.accessToken}')}")
  private String scoreAuthorizationHeader;

  @Bean
  public StorageService storageService(){
    return createStorageService(new RestTemplate(), retryTemplate, storageUrl, validationService, scoreAuthorizationHeader);
  }

}
