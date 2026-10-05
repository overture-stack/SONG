package bio.overture.song.server.controller;

import static bio.overture.song.server.utils.EndpointTester.createEndpointTester;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.WebApplicationContext;

/** Tests GET /isAlive. */
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class HealthControllerTest {

  @Autowired private WebApplicationContext webApplicationContext;

  @Test
  public void isAlive_returnsTrue() {
    val endpointTester =
        createEndpointTester(webAppContextSetup(webApplicationContext).build(), true);
    val response = endpointTester.initWebRequest().endpoint("isAlive").getAnd().assertOk();
    assertEquals("true", response.getResponse().getBody());
  }
}
