package bio.overture.song.sdk.errors;

import static java.lang.String.format;

public class ManifestClientException extends RuntimeException {

  public ManifestClientException() {
    super();
  }

  public ManifestClientException(String message) {
    super(message);
  }

  public ManifestClientException(String message, Throwable cause) {
    super(message, cause);
  }

  public ManifestClientException(Throwable cause) {
    super(cause);
  }

  public static void checkManifest(boolean expression, String formattedString, Object... args) {
    if (!expression) {
      throw new ManifestClientException(format(formattedString, args));
    }
  }
}
