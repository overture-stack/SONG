/*
 * Copyright (c) 2019. Ontario Institute for Cancer Research
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
package bio.overture.song.sdk.model;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;
import static java.util.Objects.isNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import lombok.Data;
import lombok.NonNull;
import lombok.val;

@Data
public class Manifest {

  // Refer to [DCC-5706] for two tabs reason
  private static final String TWO_TABS = "\t\t";
  private static final String NEWLINE = "\n";
  private static final String EMPTY = "";

  private final String analysisId;
  private final Collection<ManifestEntry> entries;

  public Manifest(@NonNull String analysisId) {
    this.analysisId = analysisId;
    this.entries = new ArrayList<>();
  }

  public void add(ManifestEntry m) {
    entries.add(m);
  }

  @Override
  public String toString() {
    return analysisId
        + TWO_TABS
        + NEWLINE
        + entries.stream().map(e -> e.toString() + NEWLINE).reduce(EMPTY, (a, b) -> a + b);
  }

  public void writeToFile(@NonNull String outputFilename) throws IOException {
    val path = Paths.get(outputFilename);
    val parentPath = path.getParent();
    if (!isNull(parentPath) && !exists(parentPath)) {
      createDirectories(path.getParent());
    }
    Files.write(path, toString().getBytes());
  }

  public void addAll(@NonNull Collection<? extends ManifestEntry> collect) {
    entries.addAll(collect);
  }
}
