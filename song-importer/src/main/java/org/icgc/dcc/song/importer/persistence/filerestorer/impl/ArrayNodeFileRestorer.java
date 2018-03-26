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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import org.icgc.dcc.song.importer.persistence.filerestorer.FileRestorer;

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
