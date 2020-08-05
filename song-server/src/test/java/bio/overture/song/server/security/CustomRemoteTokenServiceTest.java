package bio.overture.song.server.security;

import bio.overture.song.server.JWTTestConfig;
import bio.overture.song.server.utils.jwt.JWTGenerator;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static bio.overture.song.server.utils.jwt.JWTGenerator.generateDummyUserContext;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class CustomRemoteTokenServiceTest {
  private static final String API_KEY = UUID.randomUUID().toString();

  private CustomResourceServerTokenServices customResourceServerTokenServices;
  @Mock private RemoteTokenServices remoteTokenServices;
  @Mock private TokenStore tokenStore;
  private RetryTemplate retryTemplate = new RetryTemplate();
  private JWTGenerator jwtGenerator;


  @Before
  public void beforeTest(){
    val c = new JWTTestConfig();
    jwtGenerator = new JWTGenerator(c.keyPair());
    customResourceServerTokenServices = new CustomResourceServerTokenServices(remoteTokenServices, tokenStore, retryTemplate);
  }

  @Test
  public void accessTokenResolution_apiKey_success(){
    when(remoteTokenServices.loadAuthentication(API_KEY)).thenReturn(null);
    when(remoteTokenServices.readAccessToken(API_KEY)).thenReturn(null);
    customResourceServerTokenServices.loadAuthentication(API_KEY);
    customResourceServerTokenServices.readAccessToken(API_KEY);
    verify(remoteTokenServices, times(1)).loadAuthentication(API_KEY);
    verify(remoteTokenServices, times(1)).readAccessToken(API_KEY);
  }

  @Test
  public void accessTokenResolution_jwt_success(){
    val jwtString  = jwtGenerator.generateValidUserJwt(generateDummyUserContext(List.of("song.WRITE")));
    when(tokenStore.readAuthentication(jwtString)).thenReturn(null);
    when(tokenStore.readAccessToken(jwtString)).thenReturn(null);
    customResourceServerTokenServices.loadAuthentication(jwtString);
    customResourceServerTokenServices.readAccessToken(jwtString);
    verify(tokenStore, times(1)).readAuthentication(jwtString);
    verify(tokenStore, times(1)).readAccessToken(jwtString);
  }

}
