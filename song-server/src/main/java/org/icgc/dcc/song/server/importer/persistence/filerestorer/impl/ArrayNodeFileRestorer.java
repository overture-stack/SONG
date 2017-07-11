package org.icgc.dcc.song.server.importer.persistence.filerestorer.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import org.icgc.dcc.song.server.importer.persistence.filerestorer.FileRestorer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkState;

@RequiredArgsConstructor
public class ArrayNodeFileRestorer implements FileRestorer<ArrayNode, String> {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final Path outputDir;

  @Override public ArrayNode restore(String s) throws IOException, ClassNotFoundException {
    checkState(isPersisted(s),
        "Cannot restore JSON if persistedFilename [%s] DNE", getPersistedPath(s).toString());
    return (ArrayNode)OBJECT_MAPPER.readTree(getPersistedPath(s).toFile());
  }

  @Override public void store(ArrayNode jsonNode, String s) throws IOException {
    OBJECT_MAPPER.writeValue(getPersistedPath(s).toFile(), jsonNode);
  }

  @Override public void clean(String s) throws IOException {
    Files.deleteIfExists(getPersistedPath(s));
  }

  @Override public boolean isPersisted(String s) {
    return Files.exists(getPersistedPath(s));
  }

  @Override public Path getPersistedPath(String s) {
    return outputDir.resolve(s);
  }

  public static ArrayNodeFileRestorer createJsonFileRestorer(Path outputDir) {
    return new ArrayNodeFileRestorer(outputDir);
  }

}
