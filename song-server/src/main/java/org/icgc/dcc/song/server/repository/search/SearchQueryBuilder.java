package org.icgc.dcc.song.server.repository.search;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.icgc.dcc.song.server.model.enums.InfoSearchResponseColumns.analysis_id;
import static org.icgc.dcc.song.server.model.enums.InfoSearchResponseColumns.info;
import static org.icgc.dcc.song.server.repository.search.SearchTerm.createSearchTerm;

/**
 * Mutable class that builds a search query for greedy regex searching key-value pairs in a table
 */
@RequiredArgsConstructor
public class SearchQueryBuilder {

  private static final String TABLE_NAME = "info";
  private static final String AND_DELIMITER = " AND ";
  private static final String JSON_OBJECT_ARROW = "->";
  private static final String JSON_VALUE_ARROW = "->>";
  private static final String REGEX_ASSIGNMENT = " ~ ";
  private static final String STATEMENT_END = ";";

  private final boolean includeInfoField;
  private final Set<SearchTerm> searchTerms = newHashSet();

  /**
   * Builds the query based on the current configuration
   * @return postgresql query string
   */
  public String build() {
    val query = generateSelectBaseQuery(includeInfoField);
    if (searchTerms.size() > 0){
      return query + AND_DELIMITER + generateWhereConditions() + STATEMENT_END;
    } else {
      return query + STATEMENT_END;
    }
  }

  /**
   * Adds a search term
   * @param searchTerm
   * @return this
   */
  public SearchQueryBuilder add(@NonNull SearchTerm searchTerm){
    this.searchTerms.add(searchTerm);
    return this;
  }

  /**
   * Adds a search term using a key-value pair as the argument
   * @param key String representing a hierarchy of keys separated by a DOT. I.e  key1.key2.key3
   * @param value greedy regex search value for the specified key
   * @return this
   */
  public SearchQueryBuilder add(@NonNull String key, @NonNull String value){
    return add(createSearchTerm(key, value));
  }

  public SearchQueryBuilder add(@NonNull String key, @NonNull List<String> values){
    values.forEach(v -> add(key, v));
    return this;
  }

  private String generateWhereConditions(){
    return searchTerms.stream()
        .map(x -> convertToWhereCondition(TABLE_NAME, info.name(), x))
        .collect(joining(AND_DELIMITER));
  }

  public static SearchQueryBuilder createSearchQueryBuilder(boolean includeInfoField){
    return new SearchQueryBuilder(includeInfoField);
  }

  private static String convertToWhereCondition(String tableName, String columnName, SearchTerm searchTerm){
    val sb = new StringBuilder();
    sb.append(tableName+"."+columnName);
    searchTerm.getNonLeafKeys().forEach(key -> sb.append(JSON_OBJECT_ARROW).append(surroundSingleQuotes(key)));
    sb.append(JSON_VALUE_ARROW)
        .append(surroundSingleQuotes(searchTerm.getLeafKey()))
        .append(REGEX_ASSIGNMENT)
        .append(surroundSingleQuotes(searchTerm.getValue()));
    return sb.toString();
  }

  private static String surroundSingleQuotes(String input){
    return format("'%s'", input);
  }

  private static String generateSelectBaseQuery(boolean includeInfoField){
    val sb = new StringBuilder();
    sb.append(format("SELECT analysis.id AS %s ", analysis_id.name()));
    if (includeInfoField){
      sb.append(format(", info.info AS %s ", info.name()));
    }
    sb.append(format("FROM analysis INNER JOIN %s ON analysis.id = info.id WHERE info.id_type = 'Analysis'", TABLE_NAME));
    return sb.toString();
  }

}
