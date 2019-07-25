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

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class AnalysisTypePageableResolver implements HandlerMethodArgumentResolver {

  public static final String SORT = "sort";
  public static final String SORTORDER = "sortOrder";
  public static final String OFFSET = "offset";
  public static final String LIMIT = "limit";

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
    // get paging parameters
    String limit = nativeWebRequest.getParameter(LIMIT);
    String offset = nativeWebRequest.getParameter(OFFSET);
    String sort = nativeWebRequest.getParameter(SORT);
    String sortOrder = nativeWebRequest.getParameter(SORTORDER);

    return AnalysisTypePageable.builder()
        .limit(limit)
        .offset(offset)
        .sort(sort)
        .sortOrder(sortOrder)
        .build();
  }

  public Pageable getPageable() {
    return AnalysisTypePageable.builder()
        .limit(null)
        .offset(null)
        .sort(null)
        .sortOrder(null)
        .build();
  }
}
