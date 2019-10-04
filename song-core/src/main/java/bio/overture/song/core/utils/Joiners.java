package bio.overture.song.core.utils;

import static lombok.AccessLevel.PRIVATE;

import com.google.common.base.Joiner;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class Joiners {
  public static final Joiner AMPERSAND = Joiner.on("&");
  public static final Joiner NEWLINE = Joiner.on("\n");
  public static final Joiner COMMA = Joiner.on(",");
  public static final Joiner SLASH = Joiner.on("/");
  public static final Joiner PATH = SLASH;
}
