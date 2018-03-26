/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.icgc.dcc.song.importer.persistence.filerestorer.impl;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.importer.persistence.ObjectPersistance;
import org.icgc.dcc.song.importer.persistence.filerestorer.FileRestorer;

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
