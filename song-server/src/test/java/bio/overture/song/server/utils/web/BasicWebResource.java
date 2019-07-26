package bio.overture.song.server.utils.web;

import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

public class BasicWebResource<T, O extends ResponseOption<T, O>>
    extends AbstractWebResource<T, O, BasicWebResource<T, O>> {

  public BasicWebResource(MockMvc mockMvc, String serverUrl, Class<T> responseType) {
    super(mockMvc, serverUrl, responseType);
  }

  @Override
  protected O createResponseOption(ResponseEntity<T> responseEntity) {
    return (O) new ResponseOption<T, O>(responseEntity);
  }
}
