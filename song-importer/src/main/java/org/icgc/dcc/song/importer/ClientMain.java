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
package org.icgc.dcc.song.importer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Configuration;

import static org.icgc.dcc.common.core.util.Joiners.NEWLINE;
import static org.springframework.boot.Banner.Mode.CONSOLE;

/**
 * Application entry point.
 */
@Slf4j
@SpringBootApplication
@Configuration
@RequiredArgsConstructor
public class ClientMain implements CommandLineRunner {

  @Autowired private Importer importer;

  @Override public void run(String... strings) throws Exception {
    try{
      log.info("Importer Started");
      importer.run();
      log.info("Importer finished");
    } catch (Throwable e){
      log.error("Failed to run Importer with exception [{}] : [Message] -- {}:\n{} ",
          e.getClass().getSimpleName(), e.getMessage(), NEWLINE.join(e.getStackTrace()));
    }
  }

  public static void main(String... args) {
    val app = new SpringApplicationBuilder(ClientMain.class)
        .bannerMode(CONSOLE)
        .web(false)
        .logStartupInfo(false)
        .registerShutdownHook(true)
        .addCommandLineProperties(true)
        .build();
    app.run(args);


  }

}
