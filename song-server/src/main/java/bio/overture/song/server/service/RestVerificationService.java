package bio.overture.song.server.service;

import bio.overture.song.server.model.dto.VerifierReply;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

public class RestVerificationService implements RemoteVerificationService, VerificationService {
  RestTemplate template;
  String url;

  public RestVerificationService(RestTemplate template, String url) {
    this.template = template;
    this.url = url;
  }

  @SneakyThrows
  public VerifierReply getReply(String message) {
    val h = new HttpHeaders();
    h.setContentType(MediaType.APPLICATION_JSON_UTF8);
    val entity = new HttpEntity<String>(message, h);

    val reply = template.postForObject(url, entity, VerifierReply.class);
    return reply;
  }
}
