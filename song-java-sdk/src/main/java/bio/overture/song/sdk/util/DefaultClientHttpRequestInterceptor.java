/*
 * Copyright (c) 2019. Ontario Institute for Cancer Research
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

package bio.overture.song.sdk.util;

import static com.google.common.collect.Lists.newArrayList;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;

import java.io.IOException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

@RequiredArgsConstructor
public class DefaultClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

  @NonNull private final String accessToken;

  @Override
  public ClientHttpResponse intercept(
      HttpRequest httpRequest, byte[] bytes, ClientHttpRequestExecution clientHttpRequestExecution)
      throws IOException {
    val h = httpRequest.getHeaders();
    h.remove(CONTENT_TYPE);
    h.remove(ACCEPT);
    h.setAccept(newArrayList(APPLICATION_JSON_UTF8));
    h.setContentType(APPLICATION_JSON_UTF8);
    h.setBearerAuth(accessToken);
    return clientHttpRequestExecution.execute(httpRequest, bytes);
  }
}
