package org.icgc.dcc.song.importer.download.queries;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.icgc.dcc.common.core.json.JsonNodeBuilders;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.icgc.dcc.common.core.json.JsonNodeBuilders.array;

public interface PortalQuery {

  ObjectNode buildQuery();

  static ObjectNode createIs(String... values){
    return createIs(newArrayList(values));
  }

  static ObjectNode createIs(List<String> list){
    return JsonNodeBuilders.object()
        .with("is",
            array()
                .with(list)
                .end())
        .end();
  }
}
