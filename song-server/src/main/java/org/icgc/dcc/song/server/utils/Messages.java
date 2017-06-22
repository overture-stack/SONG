package org.icgc.dcc.song.server.utils;

import static java.lang.String.format;

public class Messages {

  private static final String OK = "OK";

  public static String contextMessage(String context, String formattedMessage, Object...args){
    return format(format("[%s]: %s", context, formattedMessage), args);
  }

  public static String contextMessage(Class<?> clazz, String formattedMessage, Object...args){
    return contextMessage(clazz.getSimpleName(), formattedMessage, args);
  }

}
