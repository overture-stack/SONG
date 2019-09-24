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

import bio.overture.song.core.model.FileUpdateRequest;
import bio.overture.song.core.model.enums.AccessTypes;
import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.sdk.config.RestClientConfig;
import bio.overture.song.sdk.register.Registry;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.IOException;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static bio.overture.song.client.cli.Status.statusFromResponse;
import static bio.overture.song.core.model.enums.AccessTypes.resolveAccessType;

@RequiredArgsConstructor
@Parameters(separators = "=", commandDescription = "Update a file")
public class FileUpdateCommand extends Command {

  @Parameter(
      names = {"--object-id"},
      required = true)
  private String objectId;

  @Parameter(names = {"-s", "--size"})
  private Long fileSize;

  @Parameter(names = {"-m", "--md5"})
  private String fileMd5;

  @Parameter(
      names = {"-a", "--access"},
      converter = AccessTypeConverter.class)
  private AccessTypes fileAccess;

  @Parameter(
      names = {"-i", "--info"},
      converter = JsonNodeConverter.class)
  private JsonNode fileInfoString;

  @NonNull private RestClientConfig config;

  @NonNull private Registry registry;

  @Override
  public void run() throws IOException {
    val request =
        FileUpdateRequest.builder()
            .fileSize(fileSize)
            .fileAccess(isNull(fileAccess) ? null : fileAccess.toString())
            .fileMd5sum(fileMd5)
            .info(fileInfoString)
            .build();

    val response = registry.updateFile(config.getStudyId(), objectId, request);
    val status = statusFromResponse(response);
    save(status);
  }

  public static class AccessTypeConverter implements IStringConverter<AccessTypes> {

    @Override
    public AccessTypes convert(String s) {
      try {
        return resolveAccessType(s);
      } catch (Throwable e) {
        throw new ParameterException(e);
      }
    }
  }

  public static class JsonNodeConverter implements IStringConverter<JsonNode> {

    @Override
    public JsonNode convert(String s) {
      try {
        return JsonUtils.readTree(s);
      } catch (IOException e) {
        throw new ParameterException(format("Could not parse the JSON info string: %s", s));
      }
    }
  }
}
