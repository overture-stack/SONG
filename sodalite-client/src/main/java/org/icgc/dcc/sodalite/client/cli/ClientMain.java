package org.icgc.dcc.sodalite.client.cli;

import org.icgc.dcc.sodalite.client.command.Command;
import org.icgc.dcc.sodalite.client.config.SodaliteConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.*;
import org.springframework.stereotype.*;

@Component
public class ClientMain implements CommandLineRunner{
  SodaliteConfig config;
  
  @Autowired
  ClientMain(SodaliteConfig config) {
	  this.config = config;
  }
  
  public void run(String... args) {
    Command c = Command.parse(args);
    c.run(config);
    c.getStatus().report();
  }

}
