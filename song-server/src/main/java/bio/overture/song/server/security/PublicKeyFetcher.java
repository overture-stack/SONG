package bio.overture.song.server.security;

@FunctionalInterface
public interface PublicKeyFetcher {

  //  // TODO: [rtisma] add error handling
  String getPublicKey();
}
