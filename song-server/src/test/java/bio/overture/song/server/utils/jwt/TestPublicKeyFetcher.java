package bio.overture.song.server.utils.jwt;

import bio.overture.song.server.security.PublicKeyFetcher;
import java.security.KeyPair;
import java.util.Base64;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"test", "jwt"})
public class TestPublicKeyFetcher implements PublicKeyFetcher {

  private final KeyPair keyPair;

  @Autowired
  public TestPublicKeyFetcher(@NonNull KeyPair keyPair) {
    this.keyPair = keyPair;
  }

  @Override
  public String getPublicKey() {
    return convertToPublicKeyWithHeader(getDecodedPublicKey());
  }

  public String getDecodedPublicKey() {
    return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
  }

  private static String convertToPublicKeyWithHeader(String key) {
    StringBuilder result = new StringBuilder();
    result.append("-----BEGIN PUBLIC KEY-----\n");
    result.append(key);
    result.append("\n-----END PUBLIC KEY-----");
    return result.toString();
  }
}
