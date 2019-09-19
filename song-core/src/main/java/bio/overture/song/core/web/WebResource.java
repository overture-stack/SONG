package bio.overture.song.core.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public interface WebResource {

  ResponseEntity<String> executeRequest(
      HttpMethod httpMethod, String url, HttpHeaders headers, String body);
}
