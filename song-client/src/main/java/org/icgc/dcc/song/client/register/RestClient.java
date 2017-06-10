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

import org.icgc.dcc.song.client.cli.Status;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import lombok.val;

/**
 * 
 */
public class RestClient {

  private RestTemplate rest;

  RestClient() {
    this.rest = new RestTemplate();
  }

  Status get(String url) {
    val status = new Status();
    val response = rest.getForEntity(url, String.class);
    if (response.getStatusCode() == HttpStatus.OK) {
      if (response.getBody() == null) {
        status.err("Null response from server");
      } else {
        status.output(response.getBody().toString());
      }
    } else {
      status.err(response.toString());
    }
    return status;
  }

  public Status post(String url) {
    return post(url, "");
  }

  public Status post(String url, String json) {
    Status status = new Status();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
    HttpEntity<String> entity = new HttpEntity<String>(json, headers);

    val response = rest.postForEntity(url, entity, String.class);
    if (response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
      status.output(response.getBody());
    } else {
      status.err(response.toString());
    }
    return status;
  }

  public Status put(String url, String json) {
    Status status = new Status();
    try {
      rest.put(url, json);
    } catch (RestClientException e) {
      status.err(e.getMessage());
    }
    return status;
  }

}
