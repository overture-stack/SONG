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

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.NameBasedGenerator;
import java.security.MessageDigest;
import java.util.UUID;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties(prefix = "id")
public class IdConfig {
  private static final UUID NAMESPACE_UUID =
      UUID.fromString("6ba7b812-9dad-11d1-80b4-00c04fd430c8");

  @Bean
  public NameBasedGenerator nameBasedGenerator() {
    return createNameBasedGenerator();
  }

  @SneakyThrows
  public static NameBasedGenerator createNameBasedGenerator() {
    return Generators.nameBasedGenerator(NAMESPACE_UUID, MessageDigest.getInstance("SHA-1"));
  }
}
