package org.icgc.dcc.sodalite.client.cli;

import org.icgc.dcc.sodalite.client.command.Command;
import org.icgc.dcc.sodalite.client.config.SodaliteConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.val;

@Component
public class ClientMain implements CommandLineRunner {

  private SodaliteConfig config;

  @Autowired
  ClientMain(SodaliteConfig config) {
    this.config = config;
  }

  @Override
  public void run(String... args) {
    val c = Command.parse(args);
    c.run(config);
    c.getStatus().report();
  }

}
