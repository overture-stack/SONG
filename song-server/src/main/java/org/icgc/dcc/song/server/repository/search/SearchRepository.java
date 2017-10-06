package org.icgc.dcc.song.server.repository.search;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.skife.jdbi.v2.Handle;

import java.util.List;

import static org.icgc.dcc.song.server.repository.mapper.InfoSearchResponseMapper.createInfoSearchResponseMapper;
import static org.icgc.dcc.song.server.repository.search.SearchQueryBuilder.createSearchQueryBuilder;

@RequiredArgsConstructor
public class SearchRepository {

  private final Handle handle;

  public List<InfoSearchResponse> infoSearch(boolean includeInfo, @NonNull Iterable<SearchTerm> searchTerms){
    val infoSearchResponseMapper = createInfoSearchResponseMapper(includeInfo);
    val searchQueryBuilder = createSearchQueryBuilder(includeInfo);
    searchTerms.forEach(searchQueryBuilder::add);
    val query = searchQueryBuilder.build();
    return handle.createQuery(query)
        .cleanupHandle()
        .map(infoSearchResponseMapper)
        .list();
  }

}
