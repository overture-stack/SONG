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
package org.icgc.dcc.song.client.config;

import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.client.errors.ServerResponseErrorHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.Boolean.parseBoolean;
import static lombok.AccessLevel.NONE;

/**
 * 
 */
@Slf4j
@ConfigurationProperties(prefix = "client")
@Component
@Data
public class Config {

  @Value("client.serverUrl")
  private String serverUrl;
  @Value("client.studyId")
  private String studyId;
  @Value("client.programName")
  private String programName;
  @Value("client.accessToken")
  private String accessToken;

  @Getter(NONE)
  @Value("client.debug")
  private String debug;

  @Value("${client.proxy.host}")
  private String proxyHost;

  @Value("${client.proxy.port}")
  private String proxyPort;

  @Bean
  public RestTemplate restTemplate(){
    val restTemplate = new RestTemplate(clientHttpRequestFactory());
    restTemplate.setErrorHandler(new ServerResponseErrorHandler());
    return restTemplate;
  }

  public boolean isDebug(){
    return parseBoolean(debug);
  }

  private Boolean hasProxy(){
    val isProxyPortDefined = !isNullOrEmpty(proxyPort);
    val isProxyHostDefined = !isNullOrEmpty(proxyHost);
    checkState(isProxyHostDefined == isProxyPortDefined,
        "The proxy parameters proxyHost=%s and proxyPort=%s must be mutually defined or undefined",
        proxyHost, proxyPort);
    return isProxyHostDefined;
  }

  private SimpleClientHttpRequestFactory clientHttpRequestFactory(){
    val reqFactory = new SimpleClientHttpRequestFactory();
    if (hasProxy()){

      log.warn("[Proxy Enabled] ProxyHost={}   ProxyPort={}", proxyHost, proxyPort);
      val proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort)));
      reqFactory.setProxy(proxy);
    }
    return reqFactory;
  }


}
