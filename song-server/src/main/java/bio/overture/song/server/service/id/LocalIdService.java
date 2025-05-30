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

package bio.overture.song.server.service.id;

import static com.fasterxml.uuid.Generators.randomBasedGenerator;

import com.fasterxml.uuid.impl.NameBasedGenerator;
import com.fasterxml.uuid.impl.RandomBasedGenerator;
import com.google.common.base.Joiner;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Local implementation of the IdService, that does not require an external REST service for
 * registering canonical IDs. Uses the database for ID resolution.
 */
@Slf4j
public class LocalIdService implements IdService {

  /** Constants */
  private static final Joiner COLON = Joiner.on(":");

  private static final RandomBasedGenerator RANDOM_UUID_GENERATOR = randomBasedGenerator();

  /** Dependencies */
  private final NameBasedGenerator nameBasedGenerator;

  public LocalIdService(@NonNull NameBasedGenerator nameBasedGenerator) {
    this.nameBasedGenerator = nameBasedGenerator;
  }

  @Override
  public String generateAnalysisId() {
    return RANDOM_UUID_GENERATOR.generate().toString();
  }

  @Override
  public Optional<String> getFileId(@NonNull String analysisId, @NonNull String fileName) {
    return generateId(analysisId, fileName);
  }

  private Optional<String> generateId(String... keys) {
    return Optional.of(nameBasedGenerator.generate(COLON.join(keys)).toString());
  }
}
