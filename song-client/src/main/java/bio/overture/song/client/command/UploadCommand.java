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

import bio.overture.song.client.register.Registry;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.File;
import java.io.IOException;

@RequiredArgsConstructor
@Parameters(separators = "=", commandDescription = "Upload an analysis file, and get an upload id")
public class UploadCommand extends Command {

  @Parameter(names = { "-f", "--file" })
  private String fileName;

  @Parameter(names = { "-a", "--async" },description = "Enables asynchronous validation")
  boolean isAsyncValidation = false;

  @NonNull
  private Registry registry;

  @Override
  public void run() throws IOException {
    val json = readUploadContent();
    val status = registry.upload(json, isAsyncValidation);
    save(status);
  }

  private String readUploadContent() throws IOException {
    if (fileName == null) {
      val json=getJson();
      return json.toString();
    }

    val file = new File(fileName);
    return Files.toString(file, Charsets.UTF_8);
  }

}
