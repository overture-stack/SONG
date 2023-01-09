/*
 * Copyright (c) 2019. Ontario Institute for Cancer Research
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

package bio.overture.song.server;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

@Component
public class DataInitializr {
  @Autowired DataSource dataSource;

  @EventListener({ApplicationReadyEvent.class})
  public void prepareTestData() {
    ResourceDatabasePopulator databasePurger =
        new ResourceDatabasePopulator(new ClassPathResource("purge.sql"));
    databasePurger.execute(dataSource);

    ResourceDatabasePopulator databasePopulator =
        new ResourceDatabasePopulator(new ClassPathResource("data.sql"));
    databasePopulator.execute(dataSource);
  }
}
