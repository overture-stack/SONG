package org.icgc.dcc.song.server.importer.persitance.filerestorer;

import java.io.IOException;
import java.nio.file.Path;

public interface FileRestorer<T, P> {

  @SuppressWarnings("unchecked") T restore(P p) throws IOException, ClassNotFoundException;

  void store(T t, P p) throws IOException;

//  void store(List<T> iterable, P p) throws IOException;

  void clean(P p) throws IOException;

  boolean isPersisted(P p);

  Path getPersistedPath(P p);

}
