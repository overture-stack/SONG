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

package bio.overture.song.server;

import bio.overture.song.server.service.id.RestClient;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.RetryOperations;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestOperations;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerErrors.REST_CLIENT_UNEXPECTED_RESPONSE;
import static bio.overture.song.core.testing.SongErrorAssertions.assertSongError;

@RunWith(MockitoJUnitRunner.class)
public class RestClientTest {

  private static final String URL = "someUrl";

  @Mock private RetryOperations retryOperations;
  @Mock private RestOperations restOperations;
  @Mock private RestClient restClient;

  @Before
  public void beforeTest(){
    reset(restClient);
  }

  @Test
  public void checkIfFound_exists_true(){
    when(restClient.get(anyString(), any())).thenReturn(ResponseEntity.ok(null));
    when(restClient.isFound(anyString())).thenCallRealMethod();
    assertTrue(restClient.isFound("something"));
  }

  @Test
  public void checkIfFound_nonExistent_false(){
    when(restClient.get(anyString(), any())).thenThrow(new HttpClientErrorException(NOT_FOUND));
    when(restClient.isFound(anyString())).thenCallRealMethod();
    assertFalse(restClient.isFound("something"));
  }

  @Test
  public void checkIfFound_nonNotFoundError_error(){
    when(restClient.get(anyString(), any())).thenThrow(new HttpClientErrorException(CONFLICT));
    when(restClient.isFound(anyString())).thenCallRealMethod();
    try{
      restClient.isFound("something");
      fail("expected error to be thrown");
    } catch (HttpStatusCodeException e){
      if (e.getStatusCode().equals(NOT_FOUND)){
        fail("was not expecting error to be a NOT_FOUND error");
      }
    } catch (Throwable e){
      fail("was not expecting a non-http error: "+e.getMessage());
    }
  }

  @Test
  public void getObject_ok_success(){
    val value = "someValue";
    when(restClient.get(URL, String.class)).thenReturn(ResponseEntity.ok(value));
    when(restClient.getObject(URL, String.class)).thenCallRealMethod();
    val result = restClient.getObject(URL, String.class);
    assertTrue(result.isPresent());
    assertEquals(value, result.get());
  }

  @Test
  public void getObject_notFound_success(){
    when(restClient.get(URL, String.class)).thenThrow(new HttpClientErrorException(NOT_FOUND));
    when(restClient.getObject(URL, String.class)).thenCallRealMethod();
    val result = restClient.getObject(URL, String.class);
    assertFalse(result.isPresent());
  }

  @Test
  public void getObject_nullResponse_REST_CLIENT_UNEXPECTED_RESPONSE(){
    when(restClient.get(URL, String.class)).thenReturn(null);
    when(restClient.getObject(URL, String.class)).thenCallRealMethod();
    assertSongError( () -> restClient.getObject(URL, String.class),
        REST_CLIENT_UNEXPECTED_RESPONSE);
  }

  @Test
  public void getObject_nullBody_REST_CLIENT_UNEXPECTED_RESPONSE(){
    when(restClient.get(URL, String.class)).thenReturn(ResponseEntity.ok(null));
    when(restClient.getObject(URL, String.class)).thenCallRealMethod();
    assertSongError( () -> restClient.getObject(URL, String.class),
        REST_CLIENT_UNEXPECTED_RESPONSE);
  }

  @Test
  public void getObject_nonNotFoundError_Error(){
    when(restClient.get(URL, String.class)).thenThrow(new HttpClientErrorException(CONFLICT));
    when(restClient.getObject(URL, String.class)).thenCallRealMethod();
    try{
      restClient.getObject(URL, String.class);
      fail("expected error to be thrown");
    } catch (HttpStatusCodeException e){
      if (e.getStatusCode().equals(NOT_FOUND)){
        fail("was not expecting error to be a NOT_FOUND error");
      }
    } catch (Throwable e){
      fail("was not expecting a non-http error: "+e.getMessage());
    }
  }

}
