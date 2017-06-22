package org.icgc.dcc.song.core.utils;

import org.springframework.http.ResponseEntity;

import static java.lang.String.format;

public class Responses {
  public static final String OK = "OK";

  public static ResponseEntity<String> ok(String formattedBody, Object...args){
    return ResponseEntity.ok(format(formattedBody, args));
  }

  public static String contextMessage(String context, String formattedMessage, Object...args){
    return format(format("[%s]: %s", context, formattedMessage), args);
  }

  public static String contextMessage(Class<?> clazz, String formattedMessage, Object...args){
    return contextMessage(clazz.getSimpleName(), formattedMessage, args);
  }
}
