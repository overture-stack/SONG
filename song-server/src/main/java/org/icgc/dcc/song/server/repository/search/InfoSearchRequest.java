package org.icgc.dcc.song.server.repository.search;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NonNull;
import lombok.val;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InfoSearchRequest {

  private boolean includeInfo;
  @NonNull private List<SearchTerm> searchTerms;

  @JsonIgnore
  public boolean hasSearchTerms(){
    return searchTerms.size() > 0;
  }

  public static InfoSearchRequest createInfoSearchRequest(boolean includeInfo, SearchTerm ... searchTerms){
    return createInfoSearchRequest(includeInfo, newArrayList(searchTerms));
  }

  public static InfoSearchRequest createInfoSearchRequest(boolean includeInfo, List<SearchTerm> searchTerms){
    val r = new InfoSearchRequest(searchTerms);
    r.setIncludeInfo(includeInfo);
    return r;
  }

}
