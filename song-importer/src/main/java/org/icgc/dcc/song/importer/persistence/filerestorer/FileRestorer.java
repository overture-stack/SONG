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

package org.icgc.dcc.song.importer.persistence.filerestorer;

import java.io.IOException;
import java.nio.file.Path;

public interface FileRestorer<T, I> {

  /**
   * Restores the object T from the path or identifier I
   * @param i identifier
   * @return object
   * @throws IOException
   * @throws ClassNotFoundException
   */
  @SuppressWarnings("unchecked") T restore(I i) throws IOException, ClassNotFoundException;

  /**
   * Stores the object T to the path or identifier I
   * @param t object
   * @param i identifier
   * @throws IOException
   */
  void store(T t, I i) throws IOException;

  /**
   * Cleans any objects linked to the identifier i
   * @param i
   * @throws IOException
   */
  void clean(I i) throws IOException;

  /**
   * Checks if the object linked to the identifier i is persisted
   * @param i
   * @return
   */
  boolean isPersisted(I i);

  /**
   * get the persisted Path from the identifier i
   * @param i
   * @return
   */
  Path getPersistedPath(I i);

}
