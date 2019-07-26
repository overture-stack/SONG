package bio.overture.song.server.utils.web;

import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

public class StringWebResource
    extends AbstractWebResource<String, StringResponseOption, StringWebResource> {

  public StringWebResource(MockMvc mockMvc, String serverUrl) {
    super(mockMvc, serverUrl, String.class);
  }

  @Override
  protected StringResponseOption createResponseOption(ResponseEntity<String> responseEntity) {
    return new StringResponseOption(responseEntity);
  }
}
