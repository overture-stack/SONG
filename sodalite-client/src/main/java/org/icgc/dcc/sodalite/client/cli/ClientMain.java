package org.icgc.dcc.sodalite.client.cli;

import org.icgc.dcc.sodalite.client.command.ConfigCommand;
import org.icgc.dcc.sodalite.client.command.ManifestCommand;
import org.icgc.dcc.sodalite.client.command.PublishCommand;
import org.icgc.dcc.sodalite.client.command.UploadCommand;
import org.icgc.dcc.sodalite.client.command.StatusCommand;
import org.icgc.dcc.sodalite.client.config.Config;
import org.icgc.dcc.sodalite.client.register.Registry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.val;

@Component
public class ClientMain implements CommandLineRunner {

  private CommandParser dispatcher;

  @Autowired
  ClientMain(Config config, Registry registry) {
    val programName = config.getProgramName();
    val options = new Options();

    val builder = new CommandParserBuilder(programName, options);
    builder.register("config", new ConfigCommand(config));
    builder.register("manifest", new ManifestCommand(registry, config));
    builder.register("upload", new UploadCommand(registry));
    builder.register("status", new StatusCommand(registry, config));
    builder.register("publish", new PublishCommand(registry, config));

    this.dispatcher = builder.build();
  }

  @Override
  public void run(String... args) {
    val command = dispatcher.parse(args);
    command.run();
    command.report();
  }

}
