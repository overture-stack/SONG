package org.icgc.dcc.sodalite.client.command;

import org.icgc.dcc.sodalite.client.config.Config;

import com.beust.jcommander.Parameters;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
@Parameters(commandDescription = "Show the current configuration settings")
public class ConfigCommand extends Command {

  @NonNull
  Config config;

  @Override
  public void run() {
    output("Current configuration:\n");

    @NonNull
    val url = config.getServerUrl();
    output("URL: %s\n", url);

    @NonNull
    val id = config.getStudyId();
    output("Study ID: %s\n", id);
  }

}
