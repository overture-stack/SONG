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

package bio.overture.song.core.utils;

import org.springframework.http.ResponseEntity;

import static java.lang.String.format;

public class Responses {
  public static final String OK = "OK";

  public static ResponseEntity<String> ok(String formattedBody, Object...args){
    return ResponseEntity.ok(format(formattedBody, args));
  }

  public static String contextMessage(String context, String formattedMessage, Object...args){
    return format(format("[%s] - %s", context, formattedMessage), args);
  }

  public static String contextMessage(Class<?> clazz, String formattedMessage, Object...args){
    return contextMessage(clazz.getSimpleName(), formattedMessage, args);
  }
}
