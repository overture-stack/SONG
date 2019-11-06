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

import com.fasterxml.uuid.impl.NameBasedGenerator;
import com.google.common.base.Joiner;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LocalIdService implements IdService {

  /** Constants */
  private static final Joiner COLON = Joiner.on(":");

  /** Dependencies */
  private final NameBasedGenerator nameBasedGenerator;

  @Autowired
  public LocalIdService(@NonNull NameBasedGenerator nameBasedGenerator) {
    this.nameBasedGenerator = nameBasedGenerator;
  }

  @Override
  public Optional<String> getFileId(@NonNull String analysisId, @NonNull String fileName) {
    return generateId(analysisId, fileName);
  }

  @Override
  public Optional<String> getAnalysisId(@NonNull String submitterAnalysisId, boolean create) {
    return Optional.of(submitterAnalysisId);
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
