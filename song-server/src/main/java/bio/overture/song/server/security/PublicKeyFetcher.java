package bio.overture.song.server.security;

@FunctionalInterface
public interface PublicKeyFetcher {

  String getPublicKey();
}
