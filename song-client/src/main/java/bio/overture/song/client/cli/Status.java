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
package bio.overture.song.client.cli;

import static bio.overture.song.core.utils.JsonUtils.toJson;
import static bio.overture.song.core.utils.JsonUtils.toPrettyJson;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.fusesource.jansi.Ansi.Color.GREEN;
import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.ansi;

import lombok.Data;
import lombok.NonNull;
import org.fusesource.jansi.AnsiConsole;

/** This class holds status results for commands that have run. */
@Data
public class Status {

  private static final String ERROR_PREFIX = "[SONG_CLIENT_ERROR]";

  public static String prefixErrorMessage(@NonNull String formattedErrorMessage, Object... args) {
    return ERROR_PREFIX + ": " + format(formattedErrorMessage, args);
  }

  private String errors;
  private String outputs;

  public Status() {
    this.errors = "";
    this.outputs = "";
  }

  void err(String s) {
    errors += s;
  }

  void output(String s) {
    outputs += s;
  }

  public void save(@NonNull Status s) {
    errors += s.errors;
    outputs += s.outputs;
  }

  public void outputPrettyJson(Object o) {
    output(toPrettyJson(o));
  }

  public void outputJson(Object o) {
    output(toJson(o));
  }

  public boolean isOk() {
    return errors.equals("");
  }

  public boolean hasOutputs() {
    return !outputs.equals("");
  }

  public boolean hasErrors() {
    return !isOk();
  }

  public void err(String format, Object... args) {
    err(format(format, args));
  }

  public void output(String format, Object... args) {
    if (args.length == 0) {
      outputs += format;
    } else {
      outputs += format(format, args);
    }
  }

  public void reportErrors() {
    if (!isNull(errors) && !"".equals(errors)) {
      AnsiConsole.err().println(ansi().eraseLine().fg(RED).a(errors).reset());
    }
  }

  public void reportOutput() {
    if (!isNull(outputs) && !"".equals(outputs)) {
      AnsiConsole.out().println(ansi().eraseLine().fg(GREEN).a(outputs).reset());
    }
  }

  public void report() {
    reportOutput();
    reportErrors();
  }
}
