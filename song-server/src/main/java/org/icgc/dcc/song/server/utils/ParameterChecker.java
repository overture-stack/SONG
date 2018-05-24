package org.icgc.dcc.song.server.utils;

import com.google.common.collect.ImmutableMap;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.icgc.dcc.song.core.exceptions.ServerError;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.lang.reflect.Modifier.fieldModifiers;
import static java.lang.reflect.Modifier.isPrivate;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.stream;
import static lombok.AccessLevel.PRIVATE;
import static org.icgc.dcc.common.core.util.Joiners.COMMA;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.ILLEGAL_FILTER_PARAMETER;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.ILLEGAL_QUERY_PARAMETER;
import static org.icgc.dcc.song.core.exceptions.ServerException.checkServer;

@RequiredArgsConstructor(access = PRIVATE)
public class ParameterChecker {

  @NonNull
  private final Map<Class<?>, Set<String>> map;

  public Set<String> getParameterNamesFor(Class<?> tClass){
    return map.get(tClass);
  }

  public boolean isLegal(Class<?> type, Set<String> parameterNames){
    return getParameterNamesFor(type).containsAll(parameterNames);
  }

  public void checkQueryParameters(Class<?> type, Set<String> parameterNames){
    checkParameters(type, ILLEGAL_QUERY_PARAMETER, "query", parameterNames);
  }

  public void checkFilterParameters(Class<?> type, Set<String> parameterNames){
    checkParameters(type, ILLEGAL_FILTER_PARAMETER, "filter", parameterNames);
  }

  private void checkParameters(Class<?> type, ServerError error, String parameterType, Set<String> parameterNames){
    checkServer(isLegal(type,parameterNames), getClass(),
        error,
    "The %s parameters '%s' must be a subset of the following legal %s parameters '%s' ",
        parameterType, COMMA.join(parameterNames), parameterType,
        COMMA.join(getParameterNamesFor(type)));
  }

  public static ParameterChecker createParameterChecker(Class<?> ... types) {
    return createParameterChecker(newHashSet(types));
  }

  public static ParameterChecker createParameterChecker(@NonNull Set<Class<?>> types) {
    val map = ImmutableMap.<Class<?>, Set<String>>builder();
    types.forEach(x -> map.put(x, extractFieldNames(x)));
    return new ParameterChecker(map.build());
  }

  private static Set<String> extractFieldNames(Class<?> type){
    return stream(type.getDeclaredFields())
        .filter(ParameterChecker::isBeanField)
        .map(Field::getName)
        .collect(toImmutableSet());
  }

  private static boolean isBeanField(Field field){
    val m = field.getModifiers();
    return isPrivate(m) && !isStatic(m) && ((fieldModifiers() & m) > 0);
  }

}
