package org.icgc.dcc.song.client.command;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.icgc.dcc.song.client.config.Config;
import org.icgc.dcc.song.client.register.Registry;
import org.icgc.dcc.song.core.model.enums.AccessTypes;
import org.icgc.dcc.song.core.model.file.FileUpdateRequest;
import org.icgc.dcc.song.core.utils.JsonUtils;

import java.io.IOException;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.icgc.dcc.song.core.model.enums.AccessTypes.resolveAccessType;

@RequiredArgsConstructor
@Parameters(separators = "=", commandDescription = "Update a file" )
public class FileUpdateCommand extends Command {

  @Parameter(names = { "--object-id" }, required = true)
  private String objectId;

  @Parameter(names = { "-s", "--size" })
  private Long fileSize;

  @Parameter(names = { "-m", "--md5" })
  private String fileMd5;

  @Parameter(names = { "-a", "--access" }, converter = AccessTypeConverter.class )
  private AccessTypes fileAccess;

  @Parameter(names = { "-i", "--info" }, converter = JsonNodeConverter.class )
  private JsonNode fileInfoString;

  @NonNull
  private Config config;

  @NonNull
  private Registry registry;

  @Override
  public void run() throws IOException {
    val request = FileUpdateRequest.builder()
        .fileSize(fileSize)
        .fileAccess(isNull(fileAccess) ? null : fileAccess.toString())
        .fileMd5sum(fileMd5)
        .info(fileInfoString)
        .build();
    save(registry.updateFile(config.getStudyId(), objectId, request));
  }

  public static class AccessTypeConverter implements IStringConverter<AccessTypes>{

    @Override
    public AccessTypes convert(String s) {
      try {
        return resolveAccessType(s);
      }catch (Throwable e){
        throw new ParameterException(e);
      }
    }

  }

  public static class JsonNodeConverter implements  IStringConverter<JsonNode>{

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
