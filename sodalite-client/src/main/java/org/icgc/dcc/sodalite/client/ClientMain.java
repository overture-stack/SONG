package org.icgc.dcc.sodalite.client;

import org.icgc.dcc.sodalite.client.command.Command;
import org.icgc.dcc.sodalite.client.command.Status;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ClientMain {

  public static void main(String args[]) {
    Command c = Command.parse(args);
    Status status = c.run();
    status.report();
  }

}
