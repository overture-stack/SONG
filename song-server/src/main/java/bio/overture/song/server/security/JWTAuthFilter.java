package bio.overture.song.server.security;

import bio.overture.song.server.model.JWTUser;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.filter.GenericFilterBean;

@Slf4j
public class JWTAuthFilter extends GenericFilterBean {

  private final String REQUIRED_STATUS = "APPROVED";

  @Override
  public void doFilter(
      ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
      throws IOException, ServletException {
    val authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null) {

      val details = (OAuth2AuthenticationDetails) authentication.getDetails();
      // This is specific to Ego. user.status is not in spec. This is an EGO specific spec.
      // This means, a switch should be added to enable user.status assertion (i.e an "ego"
      // profile).
      // Otherwise, this filter should be bypassed.
      if (details.getDecodedDetails() instanceof JWTUser) {
        val user = (JWTUser) details.getDecodedDetails();
        boolean hasCorrectStatus = user.getStatus().equals(REQUIRED_STATUS);

        if (!hasCorrectStatus) {
          SecurityContextHolder.clearContext();
        }
      }
    }

    filterChain.doFilter(servletRequest, servletResponse);
  }
}
