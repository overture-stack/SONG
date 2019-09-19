package bio.overture.song.client.util;

import static bio.overture.song.core.utils.CollectionUtils.isArrayBlank;
import static bio.overture.song.core.utils.CollectionUtils.isCollectionBlank;
import static bio.overture.song.core.web.QueryParam.createQueryParam;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.isNull;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;

import bio.overture.song.core.web.QueryParam;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.val;

public class RequestParamBuilder {

  private static final Joiner AMPERSAND_JOINER = Joiner.on("&");

  private List<QueryParam> queryParams = newArrayList();

  public RequestParamBuilder optionalQueryParamCollection(String key, Collection values) {
    if (!isCollectionBlank(values)) {
      return queryParam(key, values);
    }
    return this;
  }

  public RequestParamBuilder optionalQuerySingleParam(String key, Object value) {
    if (!isNull(value)) {
      return querySingleParam(key, value);
    }
    return this;
  }

  public RequestParamBuilder querySingleParam(String key, Object value) {
    return queryParam(key, ImmutableList.of(value));
  }

  public RequestParamBuilder optionalQueryParamArray(String key, Object[] values) {
    if (!isArrayBlank(values)) {
      return optionalQueryParamCollection(key, newArrayList(values));
    }
    return this;
  }

  public RequestParamBuilder optionalQueryParamMulti(String key, Object... values) {
    if (!isArrayBlank(values)) {
      return optionalQueryParamCollection(key, newArrayList(values));
    }
    return this;
  }

  public RequestParamBuilder queryParam(String key, Collection values) {
    queryParams.add(createQueryParam(key, values));
    return this;
  }

  public Optional<String> build() {
    val queryStrings = queryParams.stream().map(QueryParam::toString).collect(toImmutableSet());
    return queryStrings.isEmpty()
        ? Optional.empty()
        : Optional.of(AMPERSAND_JOINER.join(queryStrings));
  }

  public String build(@NonNull String endpoint) {
    return build().map(x -> endpoint + "?" + x).orElse(endpoint);
  }
}
