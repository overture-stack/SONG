/*
 * Copyright (c) 2017. The Ontario Institute for Cancer Research. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bio.overture.song.server.controller.analysisType;

import com.google.common.collect.ImmutableList;
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

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static lombok.AccessLevel.PRIVATE;
import static org.icgc.dcc.common.core.util.Joiners.COMMA;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.util.StringUtils.isEmpty;
import static bio.overture.song.core.exceptions.ServerErrors.MALFORMED_PARAMETER;
import static bio.overture.song.core.exceptions.ServerException.buildServerException;
import static bio.overture.song.core.exceptions.ServerException.checkServer;
import static bio.overture.song.core.exceptions.ServerException.checkServerOptional;
import static bio.overture.song.server.model.enums.ModelAttributeNames.ID;
import static bio.overture.song.server.model.enums.ModelAttributeNames.NAME;
import static bio.overture.song.server.model.enums.ModelAttributeNames.VERSION;
import static bio.overture.song.server.utils.CollectionUtils.isArrayBlank;

public class AnalysisTypePageableResolver implements HandlerMethodArgumentResolver {

  /**
   * Constants
   */
  public static final String SORT = "sort";
  public static final String SORTORDER = "sortOrder";
  public static final String OFFSET = "offset";
  public static final String LIMIT = "limit";

  public static final int DEFAULT_LIMIT = 20;
  public static final int DEFAULT_OFFSET = 0;
  public static final String DEFAULT_SORT_VARIABLE = VERSION;
  public static final Direction DEFAULT_SORT_ORDER = DESC;

  private static final String ALLOWED_SORT_VARIABLES = COMMA.join(VERSION, NAME);
  private static final String ALLOWED_DIRECTION_VARIABLES = stream(Direction.values())
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
        nativeWebRequest.getParameter(OFFSET),
        nativeWebRequest.getParameter(LIMIT),
        nativeWebRequest.getParameter(SORT),
        nativeWebRequest.getParameter(SORTORDER)
    );
  }

  public static AnalysisTypePageable createDefaultPageable() {
    return createAnalysisTypePageable(null, null, null);
  }

  public static AnalysisTypePageable createAnalysisTypePageable(@Nullable Integer offset, @Nullable Integer limit,
      @Nullable Direction sortOrder, @Nullable String... sortVariables){
    val offsetValue = resolveInteger(OFFSET, DEFAULT_OFFSET, offset);
    val limitValue = resolveInteger(LIMIT, DEFAULT_LIMIT, limit);
    val sortOrderValue = resolveSortOrder(DEFAULT_SORT_ORDER, sortOrder);
    val sortVariablesValue = resolveSortVariables(DEFAULT_SORT_VARIABLE, sortVariables);
    val sort = new Sort(sortOrderValue, sortVariablesValue);
    return new AnalysisTypePageable(offsetValue, limitValue, sort);
  }

  public static AnalysisTypePageable parseAnalysisTypePageable(@Nullable String offsetString, @Nullable String limitString,
      @Nullable String sortVariableString, @Nullable String sortOrderString){
    val offset = parseInteger(OFFSET, offsetString);
    val limit = parseInteger(LIMIT, limitString);
    val sortOrder = parseSortOrder(sortOrderString);
    val sortVariables = parseSortVariables(sortVariableString);
    return createAnalysisTypePageable(offset, limit, sortOrder, sortVariables);
  }

  private static void validateNonNegativeParam(@NonNull String paramName, int paramValue){
    checkServer(paramValue>=0, AnalysisTypePageableResolver.class,
        MALFORMED_PARAMETER,
        "The parameter '%s' with value '%s' but must be greater than 0",
        paramName, paramValue);
  }

  /**************************
   * Resolvers
   **************************/
  private static Integer resolveInteger(@NonNull String paramName, int defaultValue, @Nullable Integer value){
    if (isNull(value)){
      return defaultValue;
    } else {
      validateNonNegativeParam(paramName, value);
      return value;
    }
  }

  private static Direction resolveSortOrder(@NonNull Direction defaultSortOrder, @Nullable Direction sortOrder){
    return isNull(sortOrder) ? defaultSortOrder : sortOrder;
  }

  private static List<String> resolveSortVariables(@NonNull String defaultSortVariable, @Nullable String ... sortVariables){
    if (isArrayBlank(sortVariables)){
      return ImmutableList.of(parseVariable(defaultSortVariable));
    } else {
      return stream(sortVariables)
          .map(AnalysisTypePageableResolver::parseVariable)
          .collect(toImmutableList());
    }
  }

  /**************************
   * Parsers
   **************************/

  private static Integer parseInteger(@NonNull String paramName, @Nullable String stringValue){
    if (isEmpty(stringValue)) {
      return null;
    } else {
      try {
        return parseInt(stringValue);
      } catch (NumberFormatException e){
        throw buildServerException(AnalysisTypePageable.class, MALFORMED_PARAMETER,
            "The %s value '%s' is not an integer", paramName, stringValue);
      }
    }
  }

  private static String parseVariable(@NonNull String sortVariable){
    // Sorting by ID is equivalent to sorting by version
    if (sortVariable.equals(VERSION)){
      return ID;
    } else {
      checkServer(ALLOWED_SORT_VARIABLES.contains(sortVariable),
          AnalysisTypePageable.class, MALFORMED_PARAMETER,
          "The sort variable '%s' is not one or more of [%s]",
          sortVariable, ALLOWED_SORT_VARIABLES);
      return sortVariable;
    }

  }

  private static String[] parseSortVariables(@Nullable String sortCSV){
    return isEmpty(sortCSV) ? null : sortCSV.split(",");
  }

  private static Direction parseSortOrder(@Nullable String sortOrder){
    // set default sort direction
    Direction direction = null;
    if (!isEmpty(sortOrder)){
      val result = stream(Direction.values())
          .filter(x -> x.name().equals(sortOrder.toUpperCase()) )
          .findFirst();
      direction = checkServerOptional(result, AnalysisTypePageableResolver.class, MALFORMED_PARAMETER,
          "The sortOrder value '%s' is not one of [%s]",
          sortOrder, ALLOWED_DIRECTION_VARIABLES);
    }
    return direction;
  }

  @RequiredArgsConstructor(access = PRIVATE)
  public static class AnalysisTypePageable implements Pageable {

    /**
     * Config
     */
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
