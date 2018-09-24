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

package bio.overture.song.client.errors;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import bio.overture.song.core.exceptions.ServerException;
import bio.overture.song.core.exceptions.SongError;
import bio.overture.song.core.utils.JsonUtils;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static java.util.Objects.isNull;
import static org.icgc.dcc.common.core.util.Joiners.NEWLINE;
import static bio.overture.song.core.exceptions.ServerErrors.UNAUTHORIZED_TOKEN;
import static bio.overture.song.core.exceptions.SongError.createSongError;
import static bio.overture.song.core.exceptions.SongError.parseErrorResponse;

@Slf4j
public class ServerResponseErrorHandler extends DefaultResponseErrorHandler{

  private static final String ERROR = "error";
  private static final String INVALID_TOKEN = "invalid_token";

  @SneakyThrows
  private static boolean isInvalidToken(String error){
    val response = JsonUtils.readTree(error);
    if (response.has(ERROR)) {
      val errorValue = response.path(ERROR).textValue();
      return errorValue.equals(INVALID_TOKEN);
    }
    return false;
  }

  @Override
  public void handleError(ClientHttpResponse clientHttpResponse) throws IOException, ServerException {
    val httpStatusCode = clientHttpResponse.getStatusCode();
    val br = new BufferedReader(new InputStreamReader(clientHttpResponse.getBody()));
    val body = NEWLINE.join(br.lines().iterator());
    SongError songError = parseErrorResponse(httpStatusCode,body);
    if (isNull(songError.getErrorId()) && isInvalidToken(body)){
      songError = createSongError(UNAUTHORIZED_TOKEN,"Invalid token");
    }
    throw new ServerException(songError);
  }

}
