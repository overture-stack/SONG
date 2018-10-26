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

package bio.overture.song.server.utils;

import bio.overture.song.core.exceptions.ServerError;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static bio.overture.song.server.utils.SongErrorResultMatcher.songErrorContent;

@RequiredArgsConstructor
public class EndpointTester {

  @NonNull private final MockMvc mockMvc;

  @SneakyThrows
  public void testPostError(@NonNull String endpointPath, @NonNull String payload,
      @NonNull ServerError expectedServerError){
    this.mockMvc.perform(
        post(endpointPath)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .content(payload))
      .andExpect(songErrorContent(expectedServerError));
  }

  public static EndpointTester createEndpointTester(MockMvc mockMvc) {
    return new EndpointTester(mockMvc);
  }

}
