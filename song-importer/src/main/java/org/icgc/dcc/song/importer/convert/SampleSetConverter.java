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

package org.icgc.dcc.song.importer.convert;

import lombok.NonNull;
import lombok.val;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.importer.model.SampleSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;

public class SampleSetConverter {

  public Set<SampleSet> convertSampleSets(@NonNull List<PortalFileMetadata> portalFileMetadataList){
    return portalFileMetadataList.stream()
        .map(SampleSetConverter::convertToSampleSets)
        .flatMap(Collection::stream)
        .collect(toImmutableSet());
  }

  public static Set<SampleSet> convertToSampleSets(@NonNull PortalFileMetadata portalFileMetadata){
    val analysisId = AnalysisConverter.getAnalysisId(portalFileMetadata);
    return portalFileMetadata
        .getSampleIds()
        .stream()
        .map(sampleId -> SampleSet.createSampleEntry(analysisId, sampleId))
        .collect(toImmutableSet());
  }

  public static SampleSetConverter createSampleSetConverter() {
    return new SampleSetConverter();
  }

}
