package org.icgc.dcc.song.importer.persistence;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.importer.persistence.filerestorer.FileRestorer;

import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
public class PersistenceFactory<T, I> {

  private final FileRestorer<T, I> fileRestorer;
  private final Supplier<T> supplier;

  /**
   * Persists an object if it was stored previously, otherwise
   * runs the Supplier method or function which generates the data
   * @param identifier i
   * @return the persisted object
   */
  @SneakyThrows
  public T getObject(I identifier){
    val path = fileRestorer.getPersistedPath(identifier);
    if (fileRestorer.isPersisted(identifier)){
      log.info("Restoring {}", path);
      return fileRestorer.restore(identifier);
    } else {
      val object = supplier.get();
      log.info("Storing {} ...", path);
      fileRestorer.store(object, identifier);
      return object;
    }
  }

  public static <T, P> PersistenceFactory<T,P> createPersistenceFactory(
      FileRestorer<T, P> fileRestorer, Supplier<T> supplier) {
    return new PersistenceFactory<>(fileRestorer, supplier);
  }

}
