
package org.icgc.dcc.song.server.importer.persitance;

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
