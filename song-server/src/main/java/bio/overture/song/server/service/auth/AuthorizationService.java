package bio.overture.song.server.service.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class AuthorizationService {

  @Value("${authz.host}")
  private String authZHost;

  @Value("${authz.admin.group}")
  private String adminGroup;

  private final RestTemplate restTemplate = new RestTemplate();

  public UserDetails fetchUserDetails(String accessToken) {
    String url = authZHost + "{end_point}";

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

    HttpEntity<Void> entity = new HttpEntity<>(headers);

    try {
      ResponseEntity<UserDetails> response =
          restTemplate.exchange(url, HttpMethod.GET, entity, UserDetails.class);

      return response.getBody();
    } catch (Exception e) {
      log.error("Failed to fetch user details from AuthZ", e);
      throw new RuntimeException("Failed to fetch user details from AuthZ", e);
    }
  }

  public boolean isAdmin(UserDetails userDetails) {
    if (userDetails.getGroups() == null) return false;
    return userDetails.getGroups().contains(adminGroup);
  }

  public boolean hasWriteAccessToStudy(UserDetails userDetails, String studyId) {
    if (isAdmin(userDetails)) {
      return true;
    }

    if (userDetails.getStudyAuthorization() == null
        || userDetails.getStudyAuthorization().getEditableStudies() == null) {
      return false;
    }

    return userDetails.getStudyAuthorization().getEditableStudies().contains(studyId);
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class UserDetails {
    private String sub;

    private List<String> groups;

    @JsonProperty("study_authorization")
    private StudyAuthorization studyAuthorization;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class StudyAuthorization {
    @JsonProperty("editable_studies")
    private List<String> editableStudies;
  }
}
