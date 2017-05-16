package org.icgc.dcc.sodalite.client.command;

import org.icgc.dcc.sodalite.client.config.SodaliteConfig;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

public class ConfigCommand extends Command {
	@SneakyThrows
	@Override
	public void run(@NonNull SodaliteConfig config) {
		output("Current configuration:\n");
		
		@NonNull val url = config.getServerUrl();
		output("URL: %s\n", url);
		
		@NonNull val id = config.getStudyId();
		output("Study ID: %s\n", id);
	}

}
