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

package org.icgc.dcc.song.importer.download.queries.impl;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.icgc.dcc.song.importer.download.queries.PortalQuery;

import static org.icgc.dcc.common.core.json.JsonNodeBuilders.object;
import static org.icgc.dcc.song.importer.download.queries.PortalQuery.createIs;

@RequiredArgsConstructor
public class DefaultPortalQuery implements PortalQuery{

  @NonNull private final String repoName;

  @Override
  public ObjectNode buildQuery() {
    return object()
        .with("file",
            object()
                .with("repoName", createIs(repoName))
                .with("fileFormat", createIs("VCF", "BAM"))
                .with("experimentalStrategy", createIs("WGS", "RNA-Seq"))
        )
        .end();
  }

  public static DefaultPortalQuery createDefaultPortalQuery(String repoName) {
    return new DefaultPortalQuery(repoName);
  }

}
