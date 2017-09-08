package org.icgc.dcc.song.server.repository.search;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.icgc.dcc.song.server.repository.mapper.InfoSearchResponseMapper;
import org.skife.jdbi.v2.Handle;

import java.util.List;

import static org.icgc.dcc.song.server.repository.search.SearchQueryBuilder.createSearchQueryBuilder;

@RequiredArgsConstructor
public class SearchRepository {

  private final Handle handle;
  private final InfoSearchResponseMapper infoSearchResponseMapper;

  public List<InfoSearchResponse> infoSearch(boolean includeInfo, @NonNull Iterable<SearchTerm> searchTerms){
    val searchQueryBuilder = createSearchQueryBuilder(includeInfo);
    searchTerms.forEach(searchQueryBuilder::add);
    val query = searchQueryBuilder.build();
    val r = handle.createQuery(query);
    val q = r.map(infoSearchResponseMapper);
    val out = q.list();
    return out;
  }

}
