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
