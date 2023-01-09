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

package bio.overture.song.server.utils.web;

import static java.util.Objects.isNull;
import static org.apache.commons.lang.StringUtils.isBlank;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@Slf4j
public class MockMvcWebResource extends AbstractWebResource<MockMvcWebResource> {

  private final MockMvc mockMvc;

  public MockMvcWebResource(@NonNull String serverUrl, @NonNull MockMvc mockMvc) {
    super(serverUrl);
    this.mockMvc = mockMvc;
  }

  @Override
  @SneakyThrows
  public ResponseEntity<String> executeRequest(
      HttpMethod httpMethod, String url, HttpHeaders headers, String stringBody) {

    val mvcRequest = MockMvcRequestBuilders.request(httpMethod, url).headers(headers);
    if (!isNull(stringBody)) {
      mvcRequest.content(stringBody);
    }
    val mvcResult = mockMvc.perform(mvcRequest).andReturn();
    val mvcResponse = mvcResult.getResponse();
    val httpStatus = HttpStatus.resolve(mvcResponse.getStatus());
    String responseObject = null;
    if (httpStatus.isError()) {
      responseObject = mvcResponse.getContentAsString();
      if (isBlank(responseObject) && !isNull(mvcResult.getResolvedException())) {
        responseObject = mvcResult.getResolvedException().getMessage();
      }
    } else {
      responseObject = mvcResponse.getContentAsString();
    }
    return ResponseEntity.status(mvcResponse.getStatus()).body(responseObject);
  }
}
