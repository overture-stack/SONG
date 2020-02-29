package bio.overture.song.server.service;

import static bio.overture.song.core.exceptions.ServerErrors.PAYLOAD_VERIFICATION_FAILED;
import static bio.overture.song.core.exceptions.ServerException.buildServerException;

import bio.overture.song.core.utils.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
  public List<String> getReply(String message) {
    val h = new HttpHeaders();
    h.setContentType(MediaType.APPLICATION_JSON_UTF8);
    val entity = new HttpEntity<String>(message, h);

    val json = template.postForObject(url, entity, String.class);
    val j = JsonUtils.readTree(json);

    if (!(j.get("status").asText().equals("OK"))) {
      val errorMessage = j.get("details").asText();
      throw buildServerException(getClass(), PAYLOAD_VERIFICATION_FAILED, errorMessage);
    }

    val result = new ArrayList<String>();
    for (Iterator<JsonNode> it = j.get("details").elements(); it.hasNext(); ) {
      val issues = it.next();
      result.add(issues.textValue());
    }
    return result;
  }
}
