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

package org.icgc.dcc.song.importer.filters.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.icgc.dcc.song.importer.filters.Filter;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;
import org.springframework.stereotype.Component;

/**
 * Filters out files that map to blacklisted specimen Ids.
 */
@RequiredArgsConstructor
@Component
public class SpecimenFileFilter implements Filter<PortalFileMetadata> {

  @NonNull private final Filter<String> specimenIdFilter;

  @Override public boolean isPass(PortalFileMetadata portalFileMetadata) {
    return specimenIdFilter.passStream(portalFileMetadata.getSpecimenIds()).count() > 0;
  }

  public static SpecimenFileFilter createSpecimenFileFilter(Filter<String> specimenIdFilter) {
    return new SpecimenFileFilter(specimenIdFilter);
  }

}
