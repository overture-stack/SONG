package org.icgc.dcc.song.importer.download;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import static lombok.AccessLevel.PRIVATE;
import static org.icgc.dcc.common.core.json.JsonNodeBuilders.object;
import static org.icgc.dcc.song.importer.download.urlgenerator.UrlGenerator.createIs;


@NoArgsConstructor(access = PRIVATE)
public class PortalFilterQuerys {

   public static ObjectNode buildRepoFilter(@NonNull String repoName) {
     return object()
         .with("file",
             object()
                 .with("repoName", createIs(repoName))
                 .with("fileFormat", createIs("VCF", "BAM"))
                 .with("experimentalStrategy", createIs("WGS", "RNA-Seq"))
         )
         .end();
   }


}
