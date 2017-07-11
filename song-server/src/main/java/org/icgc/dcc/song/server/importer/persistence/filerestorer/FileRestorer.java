package org.icgc.dcc.song.server.importer.persistence.filerestorer;

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
