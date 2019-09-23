package bio.overture.song.sdk.factory;

import bio.overture.song.sdk.register.Registry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class AbstractRegistryFactory {

  public abstract Registry build();

}
