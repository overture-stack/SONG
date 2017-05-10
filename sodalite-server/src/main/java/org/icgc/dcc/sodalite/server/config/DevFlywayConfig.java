package org.icgc.dcc.sodalite.server.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DevFlywayConfig {

  @Bean
  @Profile("dev")
  public FlywayMigrationStrategy cleanMigrateStrategy() {
      FlywayMigrationStrategy strategy = new FlywayMigrationStrategy() {
          @Override
          public void migrate(Flyway flyway) {
              flyway.clean();
              flyway.migrate();
              log.info("Executed the clean");
          }
      };

      return strategy;
  }
}
