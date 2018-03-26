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
package org.icgc.dcc.song.client.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.val;
import org.icgc.dcc.song.client.cli.Status;

import java.io.IOException;

/**
 * Abstract parent class for Command objects.
 */
@Data
public abstract class Command {

  private Status status = new Status();

  /***
   * Convenience method for children to save error message
   * 
   * @param format See String.format
   * @param args
   * 
   * Formats a string and adds it to the output for the command
   */
  public Status err(String format, Object... args) {
    status.err(format, args);
    return status;
  }

  /***
   * Convenience method for child classes to save output message
   * 
   * @param format See String.format
   * @param args
   * 
   * Formats a string and adds it to the error message for the command
   */
  public void output(String format, Object... args) {
    status.output(format, args);
  }

  public void save(Status status) {
    this.status.save(status);
  }

  public void report() {
    status.report();
  }

  /***
   * Require all of our children to define a "run" method.
   */
  public abstract void run() throws IOException;

  public JsonNode getJson() throws IOException {
    val mapper = new ObjectMapper();

    val json = mapper.readTree(System.in);

    return json;
  }


}
