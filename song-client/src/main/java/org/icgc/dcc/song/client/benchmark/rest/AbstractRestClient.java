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
package org.icgc.dcc.song.client.benchmark.rest;

import lombok.NonNull;
import lombok.val;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.function.Function;

import static java.util.Objects.isNull;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;

public abstract class AbstractRestClient<R> {

  public R get(@NonNull String url) {
    return _get(buildJsonContentHttpHeader(),url);
  }

  public R get(@NonNull String accessToken, String url) {
    return _get(buildAuthHttpHeader(accessToken), url);
  }

  public R post(@NonNull String url, String json) {
    return _post(buildJsonContentHttpHeader(), url, json);
  }

  public R postAuth(@NonNull String accessToken, String url, String json) {
    return _post(buildAuthJsonContentHttpHeader(accessToken), url, json);
  }

  public R postAuth(@NonNull String accessToken, String url) {
    return _post(buildAuthJsonContentHttpHeader(accessToken), url, "");
  }

  public R post(@NonNull String url) {
    return post(url, "");
  }

  public R put(String url, String json) {
    return _put(buildJsonContentHttpHeader(), url, json);
  }

  public R put(String url) {
    return put(url,"");
  }

  public R putAuth(@NonNull String accessToken, String url, String json) {
    return _put(buildAuthJsonContentHttpHeader(accessToken), url, json);
  }

  public R putAuth(@NonNull String accessToken, String url) {
    return putAuth(accessToken, url,"");
  }

  protected abstract <T> R tryRequest(Function<RestTemplate, ResponseEntity<T>> restTemplateFunction);


  private R _get(@NonNull HttpHeaders httpHeaders, @NonNull String url) {
    val entity = new HttpEntity<String>(null, httpHeaders);
    return tryRequest(x -> x.exchange(url, GET, entity, String.class));
  }

  protected R _put(@NonNull HttpHeaders httpHeaders, @NonNull String url, String json) {
    val entity = new HttpEntity<String>(json, httpHeaders);
    return tryRequest(x -> x.exchange(url, PUT, entity, String.class));
  }

  protected R _post(@NonNull HttpHeaders httpHeaders, @NonNull String url, String json){
    val entity = new HttpEntity<String>(json, httpHeaders);
    return tryRequest(x -> x.postForEntity(url, entity, String.class));
  }

  private static HttpHeaders buildJsonContentHttpHeader(){
    return buildHttpHeader(null, true);
  }

  private static HttpHeaders buildAuthHttpHeader(String accessToken ){
    return buildHttpHeader(accessToken, false);
  }

  private static HttpHeaders buildAuthJsonContentHttpHeader(String accessToken ){
    return buildHttpHeader(accessToken, true);
  }

  private static HttpHeaders buildHttpHeader(String accessToken, boolean isJsonContent ){
    val headers = new HttpHeaders();
    if (!isNull(accessToken)){
      headers.set(AUTHORIZATION, "Bearer "+accessToken);
    }
    if (isJsonContent){
      headers.setContentType(APPLICATION_JSON_UTF8);
    }
    return headers;
  }

}
