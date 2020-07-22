package bio.overture.song.server.security;

import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.server.model.JWTUser;
import com.google.common.base.Joiner;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

@Slf4j
public class JWTTokenConverter extends JwtAccessTokenConverter {

  @SneakyThrows
  public JWTTokenConverter(@NonNull String publicKey) {
    super();
    this.setVerifierKey(publicKey);
    this.afterPropertiesSet();
  }

  @Override
  public OAuth2Authentication extractAuthentication(Map<String, ?> map) {
    val context = (Map<String, ?>) map.get("context");
    val user = (Map<String, ?>) context.get("user");

    // TODO: rtisma --- this is a hack since EGO does not implement jwts correctly
    val egoScopes = (List<String>) context.get("scope");
    val mutatedMap = new HashMap<String, Object>(map);
    mutatedMap.put("scope", Joiner.on(" ").join(egoScopes));

    OAuth2Authentication authentication = super.extractAuthentication(mutatedMap);
    // TODO: rtisma --- this is also a hack. the resourceIds maps to the "aud" field. This should be
    // empty inorder for the OAuth2AuthenticationManager to process properly
    authentication.getOAuth2Request().getResourceIds().clear();
    ;
    val jwtUser = JsonUtils.mapper().convertValue(user, JWTUser.class);

    authentication.setDetails(jwtUser);

    return authentication;
  }
}
