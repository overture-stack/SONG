package bio.overture.song.server.controller;

import bio.overture.song.server.security.CustomResourceServerTokenServices;
import bio.overture.song.server.utils.jwt.TestPublicKeyFetcher;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles({"test", "secure", "jwt"})
public class JWTSecurityTest {

  @Autowired CustomResourceServerTokenServices customResourceServerTokenServices;
  @Autowired TestPublicKeyFetcher testPublicKeyFetcher;

  @Test
  public void testRob(){
    log.info("sdfsdf");
  }


}
