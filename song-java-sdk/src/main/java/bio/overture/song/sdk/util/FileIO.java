package bio.overture.song.sdk.util;

import lombok.NonNull;
import lombok.val;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;

import static com.google.common.io.Files.toByteArray;
import static java.lang.String.format;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.isRegularFile;

public class FileIO {

  public static Optional<String> statusPathDoesNotExist(@NonNull Path path) {
    if (!exists(path)) {
      return Optional.of(format("The path '%s' does not exist", path));
    }
    return Optional.empty();
  }

  public static Optional<String> statusFileDoesNotExist(@NonNull Path filepath) {
    return statusPathDoesNotExist(filepath)
        .filter(x -> !isRegularFile(filepath))
        .map(x -> format("The path '%s' is not a file", x));
  }

  public static Optional<String> statusDirectoryDoesNotExist(@NonNull Path dirpath) {
    return statusPathDoesNotExist(dirpath)
        .filter(x -> !isDirectory(dirpath))
        .map(x -> format("The path '%s' is not a directory", x));
  }

  public static void checkPathExists(@NonNull Path path) throws IOException {
    pathChecker(() -> statusPathDoesNotExist(path));
  }

  public static void checkFileExists(@NonNull Path path) throws IOException {
    pathChecker(() -> statusFileDoesNotExist(path));
  }

  public static void checkDirectoryExists(@NonNull Path path) throws IOException {
    pathChecker(() -> statusDirectoryDoesNotExist(path));
  }

  public static String readFileContent(@NonNull Path filePath) throws IOException {
    return new String(toByteArray(filePath.toFile()));
  }

  private static void pathChecker(Supplier<Optional<String>> statusSupplier) throws IOException {
    val result = statusSupplier.get();
    if (result.isPresent()){
      throw new IOException(result.get());
    }
  }

}
