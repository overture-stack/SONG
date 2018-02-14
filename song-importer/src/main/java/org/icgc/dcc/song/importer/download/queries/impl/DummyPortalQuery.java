package org.icgc.dcc.song.importer.download.queries.impl;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.icgc.dcc.song.importer.download.queries.PortalQuery;

import static org.icgc.dcc.common.core.json.JsonNodeBuilders.object;
import static org.icgc.dcc.song.importer.download.queries.PortalQuery.createIs;

@RequiredArgsConstructor
public class DummyPortalQuery implements PortalQuery{

  @NonNull private final String repoName;

  @Override
  public ObjectNode buildQuery() {
    return object()
        .with("file",
            object()
                .with("repoName", createIs(repoName))
                .with("projectCode", createIs("LICA-FR"))
                .with("specimenType", createIs("Normal - tissue adjacent to primary"))
        )
        .end();
  }

  public static DummyPortalQuery createDummyPortalQuery(String repoName) {
    return new DummyPortalQuery(repoName);
  }

}
