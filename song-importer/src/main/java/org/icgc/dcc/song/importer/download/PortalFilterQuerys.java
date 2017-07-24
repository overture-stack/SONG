package org.icgc.dcc.song.importer.download;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;
import static org.icgc.dcc.common.core.json.JsonNodeBuilders.object;
import static org.icgc.dcc.song.importer.download.urlgenerator.UrlGenerator.createIs;


@NoArgsConstructor(access = PRIVATE)
public class PortalFilterQuerys {

   public static final ObjectNode COLLAB_FILTER = object()
        .with("file",
            object()
                .with("repoName", createIs("Collaboratory - Toronto"))
                .with("fileFormat", createIs("VCF", "BAM"))
                .with("experimentalStrategy", createIs("WGS", "RNA-Seq"))
        )
        .end();


}
