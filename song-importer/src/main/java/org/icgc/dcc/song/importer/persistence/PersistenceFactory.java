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
