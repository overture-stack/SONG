/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
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

package bio.overture.song.server.utils;

import static bio.overture.song.core.exceptions.ServerErrors.ILLEGAL_FILTER_PARAMETER;
import static bio.overture.song.core.exceptions.ServerErrors.ILLEGAL_QUERY_PARAMETER;
import static bio.overture.song.core.exceptions.ServerErrors.UNREGISTERED_TYPE;
import static bio.overture.song.core.exceptions.ServerException.checkServer;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.join;
import static java.lang.reflect.Modifier.fieldModifiers;
import static java.lang.reflect.Modifier.isPrivate;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.stream;
import static lombok.AccessLevel.PRIVATE;

import bio.overture.song.core.exceptions.ServerError;
import com.google.common.collect.ImmutableMap;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(access = PRIVATE)
public class ParameterChecker {

  @NonNull private final Map<Class<?>, Set<String>> map;

  public Set<String> getFieldNamesFor(@NonNull Class<?> type) {
    checkServer(
        map.containsKey(type),
        getClass(),
        UNREGISTERED_TYPE,
        "Unregistered type '%s'",
        type.getSimpleName());
    return map.get(type);
  }

  public boolean isLegal(Class<?> type, @NonNull Set<String> parameterNames) {
    return getFieldNamesFor(type).containsAll(parameterNames);
  }

  public void checkQueryParameters(Class<?> type, Set<String> parameterNames) {
    checkParameters(type, ILLEGAL_QUERY_PARAMETER, "query", parameterNames);
  }

  public void checkFilterParameters(Class<?> type, Set<String> parameterNames) {
    checkParameters(type, ILLEGAL_FILTER_PARAMETER, "filter", parameterNames);
  }

  private void checkParameters(
      Class<?> type, ServerError error, String parameterType, Set<String> parameterNames) {
    checkServer(
        isLegal(type, parameterNames),
        getClass(),
        error,
        "The %s parameters '%s' must be a subset of the following legal %s parameters '%s' ",
        parameterType,
        join(",", parameterNames),
        parameterType,
        join(",", getFieldNamesFor(type)));
  }

  public static ParameterChecker createParameterChecker(Class<?>... types) {
    return createParameterChecker(newHashSet(types));
  }

  public static ParameterChecker createParameterChecker(@NonNull Set<Class<?>> types) {
    val map = ImmutableMap.<Class<?>, Set<String>>builder();
    types.forEach(x -> map.put(x, extractFieldNames(x)));
    return new ParameterChecker(map.build());
  }

  private static Set<String> extractFieldNames(Class<?> type) {
    return stream(type.getDeclaredFields())
        .filter(ParameterChecker::isBeanField)
        .map(Field::getName)
        .collect(toImmutableSet());
  }

  private static boolean isBeanField(Field field) {
    val m = field.getModifiers();
    return isPrivate(m) && !isStatic(m) && ((fieldModifiers() & m) > 0);
  }
}
