package org.icgc.dcc.song.server.importer.persitance;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.server.importer.persitance.filerestorer.FileRestorer;

import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
public class PersistenceFactory<T,P> {

  private final FileRestorer<T, P> fileRestorer;
  private final Supplier<T> supplier;

  @SneakyThrows
  public T getObject(P p){
    val path = fileRestorer.getPersistedPath(p);
    if (fileRestorer.isPersisted(p)){
      log.info("Restoring {}", path);
      return fileRestorer.restore(p);
    } else {
      val object = supplier.get();
      log.info("Storing {} ...", path);
      fileRestorer.store(object, p);
      return object;
    }
  }

  public static <T, P> PersistenceFactory<T,P> createPersistenceFactory(
      FileRestorer<T, P> fileRestorer, Supplier<T> supplier) {
    return new PersistenceFactory<>(fileRestorer, supplier);
  }

}
