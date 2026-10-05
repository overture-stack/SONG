package bio.overture.song.server.utils.contract;

import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.NonNull;
import lombok.val;

/**
 * Replaces the values in a JSON document that change between runs with stable placeholders, so the
 * document can be compared to a golden file.
 *
 * <p>Each string value is rewritten in this order:
 *
 * <ol>
 *   <li>every occurrence of a value registered with {@link #replaceValue} becomes {@code <name>}
 *   <li>every remaining UUID becomes {@code <uuid-N>}, numbered by first appearance in the
 *       document, so values that repeat keep the same placeholder
 *   <li>a whole value that is an ISO-8601 date-time becomes a placeholder naming its format:
 *       <ul>
 *         <li>{@code <datetime>}: no zone, such as {@code 2024-01-02T03:04:05.123456}
 *         <li>{@code <datetime-offset>}: {@code Z} or a numeric offset, such as {@code
 *             2024-01-02T03:04:05.123+00:00}
 *         <li>{@code <datetime-offset-zone-id>}: an offset followed by a zone id, as written by
 *             {@code ZonedDateTime.toString()}, such as {@code 2024-01-02T03:04:05.123Z[UTC]}
 *       </ul>
 *       Any other date format is left as is and fails the comparison.
 * </ol>
 *
 * Arrays in fields registered with {@link #sortArray} are sorted by a field of their elements
 * before any other rule applies, for arrays whose order the server does not guarantee.
 *
 * <p>Fields registered with {@link #ignoreField} have their value replaced by {@code <ignored
 * TYPE>}, which keeps the JSON type of the value but not its content. An ignored value that is a
 * date-time string still becomes its date-time placeholder.
 */
public class JsonNormalizer {

  private static final Pattern UUID_PATTERN =
      Pattern.compile(
          "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

  // Accepts 0 to 9 fractional-second digits: Jackson drops trailing zeros and Postgres stores
  // microseconds, so the digit count varies between runs.
  private static final String LOCAL_DATE_TIME =
      "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,9})?";
  private static final String OFFSET = "(Z|[+-]\\d{2}:?\\d{2})";

  /** Date-time patterns mapped to their placeholders; the patterns never match the same text. */
  private static final Map<Pattern, String> DATE_TIME_PLACEHOLDERS =
      Map.of(
          Pattern.compile("^" + LOCAL_DATE_TIME + "$"),
          "<datetime>",
          Pattern.compile("^" + LOCAL_DATE_TIME + OFFSET + "$"),
          "<datetime-offset>",
          Pattern.compile("^" + LOCAL_DATE_TIME + OFFSET + "\\[[^\\]]+\\]$"),
          "<datetime-offset-zone-id>");

  /** Known run-specific values mapped to their placeholder names, in registration order. */
  private final Map<String, String> placeholderNamesByValue = new LinkedHashMap<>();

  private final Set<String> ignoredFieldNames = new HashSet<>();

  /** Array field names mapped to the element field their elements are sorted by. */
  private final Map<String, String> sortKeysByArrayField = new HashMap<>();

  public JsonNormalizer replaceValue(@NonNull String value, @NonNull String placeholderName) {
    placeholderNamesByValue.put(value, placeholderName);
    return this;
  }

  public JsonNormalizer ignoreField(@NonNull String fieldName) {
    ignoredFieldNames.add(fieldName);
    return this;
  }

  /**
   * Sorts the elements of every array held by a field named {@code arrayFieldName} by the text of
   * their {@code sortKeyField} field. Elements without that field sort first.
   */
  public JsonNormalizer sortArray(@NonNull String arrayFieldName, @NonNull String sortKeyField) {
    sortKeysByArrayField.put(arrayFieldName, sortKeyField);
    return this;
  }

  /** Returns a normalized copy; the input is not modified. */
  public JsonNode normalize(@NonNull JsonNode document) {
    return normalizeNode(document.deepCopy(), new LinkedHashMap<>());
  }

  /** Applies the string rules to a single value; UUID numbering starts at 1 for each call. */
  public String normalizeText(@NonNull String text) {
    return normalizeString(text, new LinkedHashMap<>());
  }

  private JsonNode normalizeNode(JsonNode node, Map<String, String> uuidPlaceholders) {
    if (node.isObject()) {
      val objectNode = (ObjectNode) node;
      val normalized = JsonNodeFactory.instance.objectNode();
      val fields = objectNode.fields();
      while (fields.hasNext()) {
        val field = fields.next();
        if (ignoredFieldNames.contains(field.getKey())) {
          normalized.put(field.getKey(), ignoredValuePlaceholder(field.getValue()));
        } else if (sortKeysByArrayField.containsKey(field.getKey()) && field.getValue().isArray()) {
          val sorted =
              sortElements((ArrayNode) field.getValue(), sortKeysByArrayField.get(field.getKey()));
          normalized.set(field.getKey(), normalizeNode(sorted, uuidPlaceholders));
        } else {
          normalized.set(field.getKey(), normalizeNode(field.getValue(), uuidPlaceholders));
        }
      }
      return normalized;
    } else if (node.isArray()) {
      val normalized = JsonNodeFactory.instance.arrayNode();
      for (val element : (ArrayNode) node) {
        normalized.add(normalizeNode(element, uuidPlaceholders));
      }
      return normalized;
    } else if (node.isTextual()) {
      return TextNode.valueOf(normalizeString(node.textValue(), uuidPlaceholders));
    }
    return node;
  }

  private static ArrayNode sortElements(ArrayNode array, String sortKeyField) {
    val elements = new ArrayList<JsonNode>();
    array.forEach(elements::add);
    elements.sort(comparing(element -> element.path(sortKeyField).asText("")));
    val sorted = JsonNodeFactory.instance.arrayNode();
    elements.forEach(sorted::add);
    return sorted;
  }

  private static String ignoredValuePlaceholder(JsonNode value) {
    // A date-time string keeps its format check, so an ignored "timestamp" still fails when the
    // server changes how it writes dates.
    if (value.isTextual()) {
      val dateTimePlaceholder = findDateTimePlaceholder(value.textValue());
      if (dateTimePlaceholder != null) {
        return dateTimePlaceholder;
      }
    }
    return "<ignored " + value.getNodeType().name().toLowerCase() + ">";
  }

  /** Returns the placeholder for a date-time string, or null when the text is not a date-time. */
  private static String findDateTimePlaceholder(String text) {
    for (val entry : DATE_TIME_PLACEHOLDERS.entrySet()) {
      if (entry.getKey().matcher(text).matches()) {
        return entry.getValue();
      }
    }
    return null;
  }

  private String normalizeString(String text, Map<String, String> uuidPlaceholders) {
    var result = text;
    // Longest values first, so a value that contains another registered value is replaced whole.
    val orderedValues = new ArrayList<>(placeholderNamesByValue.keySet());
    orderedValues.sort(comparingInt(String::length).reversed());
    for (val value : orderedValues) {
      result = result.replace(value, "<" + placeholderNamesByValue.get(value) + ">");
    }

    val uuidMatcher = UUID_PATTERN.matcher(result);
    val replaced = new StringBuilder();
    while (uuidMatcher.find()) {
      val uuid = uuidMatcher.group().toLowerCase();
      val placeholder =
          uuidPlaceholders.computeIfAbsent(
              uuid, key -> "<uuid-" + (uuidPlaceholders.size() + 1) + ">");
      uuidMatcher.appendReplacement(replaced, Matcher.quoteReplacement(placeholder));
    }
    uuidMatcher.appendTail(replaced);
    result = replaced.toString();

    val dateTimePlaceholder = findDateTimePlaceholder(result);
    return dateTimePlaceholder != null ? dateTimePlaceholder : result;
  }
}
