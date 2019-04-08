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
package bio.overture.song.server.oauth;

import bio.overture.song.core.exceptions.ServerException;
import bio.overture.song.server.model.legacy.EgoIsDownException;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import static bio.overture.song.core.exceptions.ServerException.buildServerException;
import static bio.overture.song.core.exceptions.ServerErrors.UNKNOWN_ERROR;
import java.net.ConnectException;

@NoArgsConstructor
public class RetryTokenServices extends RemoteTokenServices {

  @Autowired
  private RetryTemplate retryTemplate;

  @Override
  @Cacheable("tokens")
  public OAuth2Authentication loadAuthentication(String accessToken) throws AuthenticationException,
      InvalidTokenException {
    try {
      return retryTemplate.execute(context -> RetryTokenServices.super.loadAuthentication(accessToken));
    } catch (HttpClientErrorException ex) {
      if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
        throw new InvalidTokenException(ex.getMessage());
      }
      throw (ex);
    } catch (ResourceAccessException ex2) {
      if (ex2.getRootCause() instanceof ConnectException) {
        throw new EgoIsDownException("Ego is DOWN");
      }
      throw (ex2);
    } catch (RestClientException ex3) {
      if (ex3.getCause() instanceof HttpMessageNotReadableException) {
        throw buildServerException(getClass(), UNKNOWN_ERROR,
          "Ego sent us some unreadable JSON");
      } else {
        throw buildServerException(getClass(), UNKNOWN_ERROR, ex3.getMessage());
      }
    }
  }

}
