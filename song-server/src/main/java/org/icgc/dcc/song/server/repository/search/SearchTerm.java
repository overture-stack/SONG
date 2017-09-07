package org.icgc.dcc.song.server.repository.search;

import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.val;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static org.icgc.dcc.common.core.util.Splitters.DOT;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;

/**
 * Contains a key-value pair, as well as methods for parsing the key hierarchy in to chain of keys
 */
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class SearchTerm {

  private static final List<String> EMPTY_LIST = ImmutableList.<String>builder().build();

  @NonNull private final List<String> keyChain;
  @NonNull @Getter private final String value;

  /**
   * Get the Non leaf keys of the keyChain. I.e if the keyChain is "country,province,city", then the nonleaf keys are
   * country,province
   * @return list of strings
   */
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
  public String getLeafKey() {
    return keyChain.get(keyChain.size() - 1);
  }

  public static SearchTerm createKeyValue(List<String> keyChain, String value) {
    checkArgument(keyChain.size() > 0, "There must be at least one key in the chain");
    return new SearchTerm(keyChain, value);
  }

  public static SearchTerm createKeyValue(@NonNull String key, String value) {
    val trimmedKey = key.trim();
    checkArgument(key.matches(".*\\S.*"),
        "The key '%s' is not acceptable. There must be at least one non-whitespace character", key);
    return createKeyValue(DOT.splitToList(trimmedKey), value);
  }

}
