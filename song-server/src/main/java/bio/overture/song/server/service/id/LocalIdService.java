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

import bio.overture.song.server.repository.AnalysisRepository;
import com.fasterxml.uuid.impl.NameBasedGenerator;
import com.fasterxml.uuid.impl.RandomBasedGenerator;
import com.google.common.base.Joiner;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import static com.fasterxml.uuid.Generators.randomBasedGenerator;

@Slf4j
public class LocalIdService implements IdService {

  /** Constants */
  private static final Joiner COLON = Joiner.on(":");

  private static final RandomBasedGenerator RANDOM_UUID_GENERATOR = randomBasedGenerator();

  /** Dependencies */
  private final NameBasedGenerator nameBasedGenerator;

  private final AnalysisRepository analysisRepository;

  public LocalIdService(
      @NonNull NameBasedGenerator nameBasedGenerator,
      @NonNull AnalysisRepository analysisRepository) {
    this.nameBasedGenerator = nameBasedGenerator;
    this.analysisRepository = analysisRepository;
  }

  @Override
  public boolean isAnalysisIdExist(@NonNull String analysisId) {
    return analysisRepository.existsById(analysisId);
  }

  @Override
  public Optional<String> getUniqueCandidateAnalysisId() {
    return Optional.of(RANDOM_UUID_GENERATOR.generate().toString());
  }

  @Override
  public void saveAnalysisId(@NonNull String submitterAnalysisId) {
    log.warn("Skipping analysisId creation for {}", getClass().getSimpleName());
  }

  @Override
  public Optional<String> getFileId(@NonNull String analysisId, @NonNull String fileName) {
    return generateId(analysisId, fileName);
  }

  @Override
  public Optional<String> getDonorId(@NonNull String studyId, @NonNull String submitterDonorId) {
    return generateId(studyId, submitterDonorId);
  }

  @Override
  public Optional<String> getSpecimenId(
      @NonNull String studyId, @NonNull String submitterSpecimenId) {
    return generateId(studyId, submitterSpecimenId);
  }

  @Override
  public Optional<String> getSampleId(@NonNull String studyId, @NonNull String submitterSampleId) {
    return generateId(studyId, submitterSampleId);
  }

  private Optional<String> generateId(String... keys) {
    return Optional.of(nameBasedGenerator.generate(COLON.join(keys)).toString());
  }
}
