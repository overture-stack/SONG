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
import lombok.val;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static bio.overture.song.core.exceptions.SongError.parseErrorResponse;

@RequiredArgsConstructor
public class SongErrorResultMatcher implements ResultMatcher {

  @NonNull private final ServerError expectedServerError;

  @Override public void match(MvcResult mvcResult) throws Exception {
    val expectedHttpStatus = expectedServerError.getHttpStatus();
    val body = mvcResult.getResponse().getContentAsString();
    val songError = parseErrorResponse(expectedHttpStatus, body);
    assertEquals(songError.getErrorId(),expectedServerError.getErrorId());
    assertEquals(songError.getHttpStatusName(),expectedHttpStatus.name());
    assertEquals(songError.getHttpStatusCode(),expectedHttpStatus.value());
  }

  public static SongErrorResultMatcher songErrorContent(ServerError expectedServerError){
    return new SongErrorResultMatcher(expectedServerError);
  }

}
