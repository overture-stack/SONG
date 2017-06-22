/*
 * Copyright (c) 2017 The Ontario Institute for Cancer Research. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.icgc.dcc.song.client.register;

import lombok.val;
import org.icgc.dcc.song.client.cli.Status;
import org.icgc.dcc.song.client.config.Config;
import org.icgc.dcc.song.client.errors.ServerResponseErrorHandler;
import org.icgc.dcc.song.core.exceptions.ServerException;
import org.icgc.dcc.song.core.exceptions.SongError;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.function.Function;

import static java.lang.String.format;
import static org.icgc.dcc.song.core.utils.Debug.generateHeader;

/**
 *
 */
@Component
public class RestClient {

  private static final String SONG_SERVER_ERROR_TITLE = "SONG SERVER ERROR";
  private static final int NUM_SYMBOLS = 60;
  private static final String SING_SYMBOL = " 🎵 ";
  private static final String HEADER_SYMBOL = "*";
  private static final String SONG_SERVER_ERROR_HEADER = generateHeader(SONG_SERVER_ERROR_TITLE, NUM_SYMBOLS, HEADER_SYMBOL);

  private RestTemplate restTemplate;
  private boolean debug;

  public RestClient(Config config) {
    this.restTemplate = new RestTemplate();
    this.debug = config.isDebug();
    this.restTemplate.setErrorHandler(new ServerResponseErrorHandler());
  }

  public Status get(String url) {
    return tryRequest(x -> x.getForEntity(url, String.class));
  }

  public Status post(String url) {
    return post(url, "");
  }

  public Status post(String url, String json) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
    HttpEntity<String> entity = new HttpEntity<String>(json, headers);
    return tryRequest(x -> x.postForEntity(url, entity, String.class));
  }

  private <T> Status tryRequest(Function<RestTemplate, ResponseEntity<T>> restTemplateFunction){
    Status status = new Status();
    try {
      val response = restTemplateFunction.apply(restTemplate);
      if (response.getStatusCode() == HttpStatus.OK) {
        if (response.getBody() == null) {
          status.err("Null response from server: %s", response.toString());
        } else {
          status.output(response.getBody().toString());
        }
      } else {
        status.err("[%s]: %s",response.getStatusCode().value(),response.toString());
      }
    } catch (ServerException e){
      val songError = e.getSongError();
      status.err(getSongErrorOutput(songError));
    }
    return status;
  }

  private String getSongErrorOutput(SongError songError){
    return debug ? format("%s\n%s", SONG_SERVER_ERROR_HEADER,songError.toPrettyJson()) : songError.toString();
  }

  public Status put(String url, String json) {
    Status status = new Status();
    try {
      restTemplate.put(url, json);
    } catch (ServerException e){
      val songError = e.getSongError();
      status.err(getSongErrorOutput(songError));
    }
    return status;
  }

  public Status put(String url) {
    return put(url,"");
  }


}
