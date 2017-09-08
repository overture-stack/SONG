package org.icgc.dcc.song.server.repository.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InfoSearchRequest {

  private boolean includeInfo;
  @NonNull private final List<SearchTerm> searchTerms;

}
