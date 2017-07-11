package org.icgc.dcc.song.server.importer.persistence.filerestorer.impl;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.server.importer.persistence.ObjectPersistance;
import org.icgc.dcc.song.server.importer.persistence.filerestorer.FileRestorer;

import java.io.IOException;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

import static lombok.AccessLevel.PRIVATE;

@Slf4j
@RequiredArgsConstructor(access =  PRIVATE)
public class ObjectFileRestorer<T extends Serializable> implements FileRestorer<T, String> {

  @NonNull @Getter private final Path persistedPath;
  @NonNull @Getter private final Class<T> clazz;

  private Long getSerialVersionUID(){
    return ObjectStreamClass.lookup(clazz).getSerialVersionUID();
  }

  @Override @SuppressWarnings("unchecked")
  public T restore(String name) throws IOException, ClassNotFoundException {
    if (isPersisted(name)){
      log.info("Restoring {} using SerialVersionUID {}", name, getSerialVersionUID());
      return (T) ObjectPersistance.restore(getPersistedPath(name));
    } else {
      throw new IllegalStateException(String.format("Cannot restore if persistedFilename [%s] DNE", getPersistedPath().toString()));
    }
  }

  @Override public void store(T t, String name) throws IOException {
    log.info("Storing {} using SerialVersionUID {}", name, getSerialVersionUID());
    ObjectPersistance.<T>store(t,getPersistedPath(name));
  }

  @Override public void clean(String name) throws IOException {
    Files.deleteIfExists(getPersistedPath(name));
  }

  @Override public boolean isPersisted(String name){
    return Files.exists(getPersistedPath(name));
  }

  @Override public Path getPersistedPath(String s) {
    return getSerialVersionUIDDir().resolve(s);
  }

  public static <T extends Serializable> ObjectFileRestorer<T> createObjectFileRestorer(Path persistedPath,
      Class<T> clazz) {
    return new ObjectFileRestorer<T>(persistedPath, clazz);
  }

  @SneakyThrows
  private Path getSerialVersionUIDDir(){
    val parent = persistedPath.resolve(getSerialVersionUID().toString());
    Files.createDirectories(parent);
    return parent;
  }
}
