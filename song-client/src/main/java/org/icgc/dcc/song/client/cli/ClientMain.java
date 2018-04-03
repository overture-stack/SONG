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
package org.icgc.dcc.song.client.cli;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.client.command.ConfigCommand;
import org.icgc.dcc.song.client.command.ExportCommand;
import org.icgc.dcc.song.client.command.ManifestCommand;
import org.icgc.dcc.song.client.command.PublishCommand;
import org.icgc.dcc.song.client.command.SaveCommand;
import org.icgc.dcc.song.client.command.SearchCommand;
import org.icgc.dcc.song.client.command.StatusCommand;
import org.icgc.dcc.song.client.command.SuppressCommand;
import org.icgc.dcc.song.client.command.UploadCommand;
import org.icgc.dcc.song.client.config.Config;
import org.icgc.dcc.song.client.register.ErrorStatusHeader;
import org.icgc.dcc.song.client.register.Registry;
import org.icgc.dcc.song.core.exceptions.ServerException;
import org.icgc.dcc.song.core.exceptions.SongError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.net.HttpRetryException;

import static org.icgc.dcc.song.core.exceptions.ServerErrors.UNAUTHORIZED_TOKEN;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.UNKNOWN_ERROR;
import static org.icgc.dcc.song.core.exceptions.SongError.createSongError;

@Component
@Slf4j
public class ClientMain implements CommandLineRunner {

  private CommandParser dispatcher;
  private ErrorStatusHeader errorStatusHeader;
  private Registry registry;
  private Config config;

  @Autowired
  ClientMain(Config config, Registry registry) {
    val programName = config.getProgramName();
    val options = new Options();

    val builder = new CommandParserBuilder(programName, options);
    builder.register("config", new ConfigCommand(config));
    builder.register("upload", new UploadCommand(registry));
    builder.register("status", new StatusCommand(registry, config));
    builder.register("save", new SaveCommand(registry, config));
    builder.register("search", new SearchCommand(registry, config));
    builder.register("manifest", new ManifestCommand(registry, config));
    builder.register("publish", new PublishCommand(registry, config));
    builder.register("suppress", new SuppressCommand(registry, config));
    builder.register("export", new ExportCommand(registry));
    this.dispatcher = builder.build();
    this.errorStatusHeader = new ErrorStatusHeader(config);
    this.registry = registry;
    this.config = config;
  }

  @Override
  public void run(String... args) {
    val command = dispatcher.parse(args);
    try{
      command.run();
    } catch(RestClientException e){
      val isAlive = registry.isAlive();
      SongError songError;
      if(isAlive){
        val cause = e.getCause();
        if (cause instanceof  HttpRetryException){
          val httpRetryException = (HttpRetryException)cause;
          if (httpRetryException.responseCode() == UNAUTHORIZED_TOKEN.getHttpStatus().value()){
            songError = createSongError(UNAUTHORIZED_TOKEN,"Invalid token");
          } else {
            songError = createSongError(UNKNOWN_ERROR,
                "Unknown error with ResponseCode [%s] -- Reason: %s, Message: %s",
                httpRetryException.responseCode(),
                httpRetryException.getReason(),
                httpRetryException.getMessage());
          }
        } else {
          songError = createSongError(UNKNOWN_ERROR,
              "Unknown error: %s", e.getMessage());
        }
        command.err(errorStatusHeader.getSongClientErrorOutput(songError));
      } else {
        command.err(errorStatusHeader.createMessage("The SONG server may not be running on '%s'", config.getServerUrl()));
      }
    } catch (ServerException ex) {
      val songError = ex.getSongError();
      command.err(errorStatusHeader.getSongServerErrorOutput(songError));
    } catch (IOException e) {
      command.err("IO Error: %s",e.getMessage());
    } finally{
      command.report();
    }

  }

}
