package org.icgc.dcc.sodalite.client.cli;

import java.util.HashMap;
import java.util.Map;

import org.icgc.dcc.sodalite.client.command.Command;
import org.icgc.dcc.sodalite.client.command.ConfigCommand;
import org.icgc.dcc.sodalite.client.command.ErrorCommand;
import org.icgc.dcc.sodalite.client.command.ManifestCommand;
import org.icgc.dcc.sodalite.client.command.RegisterCommand;
import org.icgc.dcc.sodalite.client.command.StatusCommand;
import org.icgc.dcc.sodalite.client.config.Config;
import org.icgc.dcc.sodalite.client.register.Registry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import lombok.val;

@Component
public class ClientMain implements CommandLineRunner {

  private Map<String, Command> commands = new HashMap<String, Command>();
  private JCommander.Builder jcBuilder;
  private JCommander jc;
  private MainCommand mc;
  private final String programName;

  @Autowired
  ClientMain(Config config, Registry registry) {
    mc = new MainCommand();
    jcBuilder = JCommander.newBuilder().addObject(mc);
    programName = config.getProgramName();
    register("config", new ConfigCommand(config));
    register("manifest", new ManifestCommand(registry, config));
    register("register", new RegisterCommand(registry));
    register("status", new StatusCommand(registry, config));
    jc = jcBuilder.build();
    jc.setProgramName(programName);
  }

  private void register(String key, Command c) {
    commands.put(key, c);
    jcBuilder.addCommand(key, c);
  }

  @Override
  public void run(String... args) {
    val command = parse(args);
    command.run();
    command.getStatus().report();
  }

  public Command parse(String[] args) {
    // String cmd;
    // // First, try to figure out which command we were given
    // jc.parseWithoutValidation(args);
    //
    // try {
    // cmd = jc.getParsedCommand();
    // } catch (ParameterException e) {
    // cmd = null;
    // }
    //
    // if (cmd == null) {
    // // we didn't enter a command, or we entered an invalid one
    // return usage();
    // }

    // Now, if we have invalid command arguments, we know to issue the
    // command specific help.
    try {
      jc.parse(args);
    } catch (ParameterException e) {
      val c = usage();
      c.err(e.toString());
      return c;
    }
    val cmd = jc.getParsedCommand();
    val c = commands.getOrDefault(cmd, new ErrorCommand("Unknown subcommand: " + cmd));
    return c;
  }

  ErrorCommand usage() {
    val buf = new StringBuilder();
    jc.usage(buf);
    return new ErrorCommand(buf.toString());
  }

  ErrorCommand usage(String command) {
    val buf = new StringBuilder();
    jc.usage(command, buf);
    return new ErrorCommand(buf.toString());
  }

}
