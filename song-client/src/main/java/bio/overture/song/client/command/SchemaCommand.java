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
package bio.overture.song.client.command;

import static bio.overture.song.client.command.rules.ModeRule.createModeRule;
import static bio.overture.song.client.command.rules.ParamTerm.createParamTerm;
import static bio.overture.song.client.command.rules.RuleProcessor.createRuleProcessor;

import bio.overture.song.client.cli.Status;
import bio.overture.song.client.register.Registry;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.IOException;
import java.util.Objects;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
@Parameters(separators = "=", commandDescription = "Retrieve schema information")
public class SchemaCommand extends Command {

  private static final String SCHEMA_ID_MODE = "SCHEMA_ID_MODE";
  private static final String LIST_MODE = "LIST_MODE";

  private static final String S_SWITCH = "-s";
  private static final String SCHEMA_ID_SWITCH = "--schema-id";
  private static final String L_SWITCH = "-l";
  private static final String LIST_SWITCH = "--list";

  @Parameter(
      names = {S_SWITCH, SCHEMA_ID_SWITCH},
      required = false)
  private String schemaId;

  @Parameter(
      names = {L_SWITCH, LIST_SWITCH},
      required = false)
  private boolean listMode = false;

  @NonNull private Registry registry;

  @Override
  public void run() throws IOException {
    val status = checkRules();
    if (!status.hasErrors()) {
      Status apiStatus;
      if (listMode) {
        apiStatus = registry.listSchemas();
      } else {
        apiStatus = registry.getSchema(schemaId);
      }
      status.save(apiStatus);
    }
    save(status);
  }

  private Status checkRules() {
    val schemaIdTerm = createParamTerm(S_SWITCH, SCHEMA_ID_SWITCH, schemaId, Objects::nonNull);
    val listTerm = createParamTerm(L_SWITCH, LIST_SWITCH, listMode, x -> x);

    val schemaIdModeRule = createModeRule(SCHEMA_ID_MODE, schemaIdTerm);
    val listModeRule = createModeRule(LIST_MODE, listTerm);
    val ruleProcessor = createRuleProcessor(schemaIdModeRule, listModeRule);
    return ruleProcessor.check();
  }
}
