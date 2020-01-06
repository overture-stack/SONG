/*
 * Copyright (c) 2019. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package bio.overture.song.server.controller.analysisType;

import static bio.overture.song.core.exceptions.ServerErrors.MALFORMED_PARAMETER;
import static bio.overture.song.core.exceptions.ServerException.buildServerException;
import static bio.overture.song.core.exceptions.ServerException.checkServer;
import static bio.overture.song.core.exceptions.ServerException.checkServerOptional;
import static bio.overture.song.core.utils.CollectionUtils.isArrayBlank;
import static bio.overture.song.server.model.enums.ModelAttributeNames.ID;
import static bio.overture.song.server.model.enums.ModelAttributeNames.NAME;
import static bio.overture.song.server.model.enums.ModelAttributeNames.VERSION;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.Integer.parseInt;
import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static lombok.AccessLevel.PRIVATE;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.util.StringUtils.isEmpty;

import bio.overture.song.server.model.enums.ModelAttributeNames;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class AnalysisTypePageableResolver implements HandlerMethodArgumentResolver {

  public static final int DEFAULT_LIMIT = 20;
  public static final int DEFAULT_OFFSET = 0;
  public static final String DEFAULT_SORT_VARIABLE = VERSION;
  public static final Direction DEFAULT_SORT_ORDER = DESC;

  private static final String ALLOWED_SORT_VARIABLES = VERSION + "," + NAME;
  private static final String ALLOWED_DIRECTION_VARIABLES =
      stream(Direction.values())
          .map(Enum::name)
          .map(x -> ImmutableList.of(x.toUpperCase(), x.toLowerCase()))
          .flatMap(Collection::stream)
          .collect(Collectors.joining(","));

  @Override
  public boolean supportsParameter(MethodParameter methodParameter) {
    return methodParameter.getParameterType().equals(Pageable.class);
  }

  @Override
  public Object resolveArgument(
      MethodParameter methodParameter,
      ModelAndViewContainer modelAndViewContainer,
      NativeWebRequest nativeWebRequest,
      WebDataBinderFactory webDataBinderFactory)
      throws Exception {
    return parseAnalysisTypePageable(
        nativeWebRequest.getParameter(ModelAttributeNames.OFFSET),
        nativeWebRequest.getParameter(ModelAttributeNames.LIMIT),
        nativeWebRequest.getParameter(ModelAttributeNames.SORT),
        nativeWebRequest.getParameter(ModelAttributeNames.SORTORDER));
  }

  public static AnalysisTypePageable createDefaultPageable() {
    return createAnalysisTypePageable(null, null, null);
  }

  public static AnalysisTypePageable createAnalysisTypePageable(
      @Nullable Integer offset,
      @Nullable Integer limit,
      @Nullable Direction sortOrder,
      @Nullable String... sortVariables) {
    val offsetValue = resolveInteger(ModelAttributeNames.OFFSET, DEFAULT_OFFSET, offset);
    val limitValue = resolveInteger(ModelAttributeNames.LIMIT, DEFAULT_LIMIT, limit);
    val sortOrderValue = resolveSortOrder(DEFAULT_SORT_ORDER, sortOrder);
    val sortVariablesValue = resolveSortVariables(DEFAULT_SORT_VARIABLE, sortVariables);
    val sort = new Sort(sortOrderValue, sortVariablesValue);
    return new AnalysisTypePageable(offsetValue, limitValue, sort);
  }

  public static AnalysisTypePageable parseAnalysisTypePageable(
      @Nullable String offsetString,
      @Nullable String limitString,
      @Nullable String sortVariableString,
      @Nullable String sortOrderString) {
    val offset = parseInteger(ModelAttributeNames.OFFSET, offsetString);
    val limit = parseInteger(ModelAttributeNames.LIMIT, limitString);
    val sortOrder = parseSortOrder(sortOrderString);
    val sortVariables = parseSortVariables(sortVariableString);
    return createAnalysisTypePageable(offset, limit, sortOrder, sortVariables);
  }

  private static void validateNonNegativeParam(@NonNull String paramName, int paramValue) {
    checkServer(
        paramValue >= 0,
        AnalysisTypePageableResolver.class,
        MALFORMED_PARAMETER,
        "The parameter '%s' with value '%s' but must be greater than 0",
        paramName,
        paramValue);
  }

  /** ************************ Resolvers ************************ */
  private static Integer resolveInteger(
      @NonNull String paramName, int defaultValue, @Nullable Integer value) {
    if (isNull(value)) {
      return defaultValue;
    } else {
      validateNonNegativeParam(paramName, value);
      return value;
    }
  }

  private static Direction resolveSortOrder(
      @NonNull Direction defaultSortOrder, @Nullable Direction sortOrder) {
    return isNull(sortOrder) ? defaultSortOrder : sortOrder;
  }

  private static List<String> resolveSortVariables(
      @NonNull String defaultSortVariable, @Nullable String... sortVariables) {
    if (isArrayBlank(sortVariables)) {
      return ImmutableList.of(parseVariable(defaultSortVariable));
    } else {
      return stream(sortVariables)
          .map(AnalysisTypePageableResolver::parseVariable)
          .collect(toImmutableList());
    }
  }

  /** ************************ Parsers ************************ */
  private static Integer parseInteger(@NonNull String paramName, @Nullable String stringValue) {
    if (isEmpty(stringValue)) {
      return null;
    } else {
      try {
        return parseInt(stringValue);
      } catch (NumberFormatException e) {
        throw buildServerException(
            AnalysisTypePageable.class,
            MALFORMED_PARAMETER,
            "The %s value '%s' is not an integer",
            paramName,
            stringValue);
      }
    }
  }

  private static String parseVariable(@NonNull String sortVariable) {
    // Sorting by ID is equivalent to sorting by version
    if (sortVariable.equals(VERSION)) {
      return ID;
    } else {
      checkServer(
          ALLOWED_SORT_VARIABLES.contains(sortVariable),
          AnalysisTypePageable.class,
          MALFORMED_PARAMETER,
          "The sort variable '%s' is not one or more of [%s]",
          sortVariable,
          ALLOWED_SORT_VARIABLES);
      return sortVariable;
    }
  }

  private static String[] parseSortVariables(@Nullable String sortCSV) {
    return isEmpty(sortCSV) ? null : sortCSV.split(",");
  }

  private static Direction parseSortOrder(@Nullable String sortOrder) {
    // set default sort direction
    Direction direction = null;
    if (!isEmpty(sortOrder)) {
      val result =
          stream(Direction.values())
              .filter(x -> x.name().equals(sortOrder.toUpperCase()))
              .findFirst();
      direction =
          checkServerOptional(
              result,
              AnalysisTypePageableResolver.class,
              MALFORMED_PARAMETER,
              "The sortOrder value '%s' is not one of [%s]",
              sortOrder,
              ALLOWED_DIRECTION_VARIABLES);
    }
    return direction;
  }

  @RequiredArgsConstructor(access = PRIVATE)
  public static class AnalysisTypePageable implements Pageable {

    /** Config */
    private final int offset;

    private final int limit;
    @NonNull private final Sort sort;

    @Override
    public int getPageNumber() {
      return 0;
    }

    @Override
    public int getPageSize() {
      return limit;
    }

    @Override
    public long getOffset() {
      return offset;
    }

    @Override
    public Sort getSort() {
      return sort;
    }

    @Override
    public Pageable next() {
      return null;
    }

    @Override
    public Pageable previousOrFirst() {
      return null;
    }

    @Override
    public Pageable first() {
      return null;
    }

    @Override
    public boolean hasPrevious() {
      return false;
    }
  }
}
