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

import lombok.Data;
import org.icgc.dcc.id.client.core.IdClient;
import org.icgc.dcc.id.client.http.HttpIdClient;
import org.icgc.dcc.id.client.util.HashIdClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties(prefix = "id")
public class IdConfig {

  private String idUrl;
  private String authToken;
  private boolean realIds;
  private boolean persistInMemory = false;

  @Bean
  public IdClient createIdClient() {
    // [SONG-167]: Temporarily removed cacheId client due to bug in DCC-ID-12: https://github.com/icgc-dcc/dcc-id/issues/12
    return realIds ? new HttpIdClient(idUrl, "", authToken) : new HashIdClient(persistInMemory);
//    return realIds ? new CachingIdClient(new HttpIdClient(idUrl, "", authToken)) : new HashIdClient(persistInMemory);
  }



}
