package org.icgc.dcc.song.server.repository.search;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.icgc.dcc.song.server.repository.search.SearchTerm.createKeyValue;

/**
 * Mutable class that builds a search query for greedy regex searching key-value pairs in a table
 */
@RequiredArgsConstructor
public class SearchQueryBuilder {

  private static final String TABLE_NAME = "info";
  private static final String COLUMN_NAME = "info";
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
    return add(createKeyValue(key, value));
  }

  private String generateWhereConditions(){
    return searchTerms.stream()
        .map(x -> convertToWhereCondition(COLUMN_NAME, x))
        .collect(joining(AND_DELIMITER));
  }

  private static String convertToWhereCondition(String columnName, SearchTerm searchTerm){
    val sb = new StringBuilder();
    sb.append(columnName);
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
    sb.append("SELECT a.id AS analysis_id ");
    if (includeInfoField){
      sb.append(format(", i.info AS %s ", COLUMN_NAME));
    }
    sb.append("FROM analysis AS a ");
    sb.append(format("INNER JOIN %s i ON a.id = i.id ", TABLE_NAME));
    sb.append("WHERE i.id_type = 'Analysis'");
    return sb.toString();
  }

}
