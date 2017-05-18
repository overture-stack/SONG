package org.icgc.dcc.sodalite.client.cli;

import java.util.HashMap;
import java.util.Map;

import org.icgc.dcc.sodalite.client.command.Command;
import org.icgc.dcc.sodalite.client.command.ConfigCommand;
import org.icgc.dcc.sodalite.client.command.ErrorCommand;
import org.icgc.dcc.sodalite.client.command.HelpCommand;
import org.icgc.dcc.sodalite.client.command.ManifestCommand;
import org.icgc.dcc.sodalite.client.command.RegisterCommand;
import org.icgc.dcc.sodalite.client.command.StatusCommand;
import org.icgc.dcc.sodalite.client.config.Config;
import org.icgc.dcc.sodalite.client.register.Registry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.val;

@Component
public class ClientMain implements CommandLineRunner {

  private Map<String, Command> commands = new HashMap<String, Command>();
  // private JCommander jc;
  private final String programName;

  @Autowired
  ClientMain(Config config, Registry registry) {
    programName = config.getProgramName();
    register("config", new ConfigCommand(config));
    register("manifest", new ManifestCommand(registry, config));
    register("register", new RegisterCommand(registry));
    register("status", new StatusCommand(registry, config));
    register("help", new HelpCommand(programName, commands.keySet()));

  }

  private void register(String key, Command c) {
    commands.put(key, c);
  }

  @Override
  public void run(String... args) {
    val c = parse(args);
    c.run(args);
    c.getStatus().report();
  }

  public Command parse(String[] args) {
    if (args.length == 0) {
      return new HelpCommand(programName, commands.keySet());
    }
    val cmd = args[0];
    val c = commands.getOrDefault(cmd, new ErrorCommand("Unknown subcommand: " + cmd));
    return c;
  }

}
