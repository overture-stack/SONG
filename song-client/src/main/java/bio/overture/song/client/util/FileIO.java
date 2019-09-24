package bio.overture.song.client.util;

import bio.overture.song.client.cli.Status;
import lombok.NonNull;
import lombok.val;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.io.Files.toByteArray;
import static java.lang.String.format;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.isRegularFile;

public class FileIO {

  private static final String ERROR_PREFIX = "[SONG_CLIENT_ERROR]";

  public static String prefixErrorMessage(@NonNull String formattedErrorMessage, Object... args) {
    return ERROR_PREFIX + ": " + format(formattedErrorMessage, args);
  }

  public static Status statusPathExists(@NonNull Path path) {
    val s = new Status();
    if (!exists(path)) {
      s.err("%s: The path '%s' does not exist", ERROR_PREFIX, path);
    }
    return s;
  }

  public static Status statusFileExists(@NonNull Path filepath) {
    val s = statusPathExists(filepath);
    if (!s.hasErrors() && !isRegularFile(filepath)) {
      s.err("%s: The path '%s' is not a file", ERROR_PREFIX, filepath);
    }
    return s;
  }

  public static Status statusDirectoryExists(@NonNull Path dirpath) {
    val s = statusPathExists(dirpath);
    if (!s.hasErrors() && !Files.isDirectory(dirpath)) {
      s.err("%s: The path '%s' is not a directory", ERROR_PREFIX, dirpath);
    }
    return s;
  }

  public static void checkPathExists(@NonNull Path path) throws IOException {
    if (!exists(path)) {
      throw new IOException(format("%s: The path '%s' does not exist", ERROR_PREFIX, path));
    }
  }

  public static void checkFileExists(@NonNull Path path) throws IOException {
    checkPathExists(path);
    if (!isRegularFile(path)) {
      throw new IOException(
          format("%s: The path '%s' exists but is not a file", ERROR_PREFIX, path));
    }
  }

  public static void checkDirectoryExists(@NonNull Path path) throws IOException {
    checkPathExists(path);
    if (!isDirectory(path)) {
      throw new IOException(
          format("%s: The path '%s' exists but is not a directory", ERROR_PREFIX, path));
    }
  }

  public static String readFileContent(@NonNull Path filePath) throws IOException {
    return new String(toByteArray(filePath.toFile()));
  }
}
