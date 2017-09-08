package org.icgc.dcc.song.server.repository.search;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

import java.util.List;

import static java.util.Collections.EMPTY_LIST;
import static org.icgc.dcc.common.core.util.Splitters.DOT;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SEARCH_TERM_SYNTAX;
import static org.icgc.dcc.song.core.exceptions.ServerException.checkServer;

/**
 * Contains a key-value pair, as well as methods for parsing the key hierarchy in to chain of keys
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchTerm {

  @NonNull @Getter private String key;
  @NonNull @Getter private String value;

  private List<String> keyChain;

  public void setKey(String key){
    this.key = key.trim();
    this.keyChain = parseKeyChain(key);
  }

  public void setValue(String value){
    checkServer(!value.equals(""),
        this.getClass(), SEARCH_TERM_SYNTAX ,
        "value for key '%s' must be non-empty and non-null", getKey());
    this.value = value;
  }

  /**
   * Get the Non leaf keys of the keyChain. I.e if the keyChain is "country,province,city", then the nonleaf keys are
   * country,province
   * @return list of strings
   */
  @JsonIgnore
  public List<String> getNonLeafKeys() {
    if (keyChain.size() < 1){
      return EMPTY_LIST;
    } else {
      return keyChain.stream()
          .limit(keyChain.size() - 1)
          .collect(toImmutableList());
    }
  }

  /**
   * Get the Leaf keys of the keyChain. I.e if the keyChain is "country,province,city", then the leaf key is "city"
   * @return string
   */
  @JsonIgnore
  public String getLeafKey() {
    return keyChain.get(keyChain.size() - 1);
  }

  private static List<String> parseKeyChain(String key){
    checkServer(key.matches(".*\\S.*"),
        SearchTerm.class, SEARCH_TERM_SYNTAX,
        "The key '%s' is not acceptable. There must be at least one non-whitespace character", key);
    return DOT.splitToList(key);
  }

  public static SearchTerm createSearchTerm(String key, String value){
    val st = new SearchTerm();
    st.setKey(key);
    st.setValue(value);
    return st;
  }

  public static List<SearchTerm> createMultiSearchTerms(@NonNull String key, @NonNull List<String> values) {
    return values.stream()
        .map(v -> createSearchTerm(key, v))
        .collect(toImmutableList());
  }

}
