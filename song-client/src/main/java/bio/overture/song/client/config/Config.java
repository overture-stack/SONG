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
package bio.overture.song.client.config;

import static java.lang.Boolean.parseBoolean;
import static lombok.AccessLevel.NONE;

import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** */
@ConfigurationProperties(prefix = "client")
@Component
@Data
public class Config {

  @Value("client.serverUrl")
  private String serverUrl;

  @Value("client.studyId")
  private String studyId;

  @Value("client.programName")
  private String programName;

  @Value("client.accessToken")
  private String accessToken;

  @Getter(NONE)
  @Value("client.debug")
  private String debug;

  public boolean isDebug() {
    return parseBoolean(debug);
  }
}
