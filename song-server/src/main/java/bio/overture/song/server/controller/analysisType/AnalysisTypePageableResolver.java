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
import lombok.val;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.stream;
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
    return buildAnalysisTypePageable(
        nativeWebRequest.getParameter(LIMIT),
        nativeWebRequest.getParameter(OFFSET),
        nativeWebRequest.getParameter(SORT),
        nativeWebRequest.getParameter(SORTORDER)

    );
  }

  public static Pageable createDefaultPageable() {
    return buildAnalysisTypePageable(null, null, null, null);
  }

  public static AnalysisTypePageable buildAnalysisTypePageable(String limitString, String offsetString,
      String sortVariableString, String sortOrderString){
    val limit = resolveInteger(limitString, LIMIT, DEFAULT_LIMIT);
    val offset = resolveInteger(offsetString, OFFSET, DEFAULT_OFFSET);
    val sortVariables = resolveSortVariables(sortVariableString, DEFAULT_SORT_VARIABLE);
    val sortOrder = resolveSortOrder(sortOrderString, DEFAULT_SORT_ORDER);
    val sort = new Sort(sortOrder, sortVariables);
    return AnalysisTypePageable.builder()
        .limit(limit)
        .offset(offset)
        .sort(sort)
        .build();
  }

  private static void validateNonNegativeParam(@NonNull String paramName, int paramValue){
    checkServer(paramValue>=0, AnalysisTypePageableResolver.class,
        MALFORMED_PARAMETER,
        "The parameter '%s' with value '%s' but must be greater than 0",
        paramName, paramValue);
  }

  private static Integer resolveInteger(String stringValue, String paramName, int defaultValue){
    if (isEmpty(stringValue)) {
      return defaultValue;
    } else {
      try {
        val result = parseInt(stringValue);
        validateNonNegativeParam(paramName, result);
        return result;
      } catch (NumberFormatException e){
        throw buildServerException(AnalysisTypePageable.class, MALFORMED_PARAMETER,
            "The %s value '%s' is not an integer", paramName, stringValue);
      }
    }
  }

  private static List<String> resolveSortVariables(String sort, String defaultSortVariable){
    if (isEmpty(sort)){
      return ImmutableList.of(defaultSortVariable);
    } else {
      return stream(sort.split(","))
          .map(x -> {
            // Sorting by ID is equivalent to sorting by version
            if (x.equals(VERSION)){
              return ID;
            } else {
              checkServer(ALLOWED_SORT_VARIABLES.contains(x),
                  AnalysisTypePageable.class, MALFORMED_PARAMETER,
                  "The sort variable '%s' is not one or more of [%s]",
                  x, ALLOWED_SORT_VARIABLES);
              return x;
            }
          })
          .collect(toImmutableList());
    }
  }

  private static Direction resolveSortOrder(String sortOrder, Direction defaultSortOrder){
    // set default sort direction
    Direction direction = defaultSortOrder;
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

}
