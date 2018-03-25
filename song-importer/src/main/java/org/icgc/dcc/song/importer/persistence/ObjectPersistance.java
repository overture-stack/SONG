
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

import lombok.Cleanup;
import lombok.NoArgsConstructor;
import lombok.val;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkState;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class ObjectPersistance {

  public static <T extends Serializable> void store(final T t, final Path pathname) throws IOException {
    @Cleanup
    val fout = new FileOutputStream(pathname.toFile());
    val oos = new ObjectOutputStream(fout);
    oos.writeObject(t);
  }

  public static Object restore(final Path pathname) throws ClassNotFoundException, IOException {
    val file = pathname.toFile();
    checkState(file.exists(), "The File[{}] DNE", pathname.toString());
    checkState(file.isFile(), "The Path[{}] is not a file", pathname.toString());
    @Cleanup
    val fin = new FileInputStream(file);
    val ois = new ObjectInputStream(fin);
    return ois.readObject();
  }

}
