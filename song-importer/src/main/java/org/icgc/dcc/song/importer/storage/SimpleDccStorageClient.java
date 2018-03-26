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
package org.icgc.dcc.song.importer.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.hash.Hashing;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.importer.config.DccStorageConfig;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static java.nio.file.Files.copy;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.icgc.dcc.common.core.json.Jackson.DEFAULT;

@Slf4j
@Component
public class SimpleDccStorageClient {


  private final DccStorageConfig dccStorageConfig;

  /**
   * State
   */
  private final Path outputDir;
  private final Path tempFile;

  public SimpleDccStorageClient(DccStorageConfig dccStorageConfig) {
    this.dccStorageConfig = dccStorageConfig;
    this.outputDir = Paths.get(dccStorageConfig.getOutputDir()).toAbsolutePath();
    initDir(outputDir);
    this.tempFile = createTempFile(outputDir);
  }

  @SneakyThrows
  public File getFile(@NonNull String objectId, @NonNull String expectedMD5Sum, @NonNull String filename) {
    val relativeFile = Paths.get(filename);
    val absFile = outputDir.resolve(relativeFile).toAbsolutePath();
    val absFilename = absFile.toString();
    checkForParentDir(absFile);
    initParentDir(absFile);

    val fileExists = Files.exists(absFile);
    val persist = dccStorageConfig.isPersist();
    val bypassMd5 = dccStorageConfig.isBypassMd5Check();
    val force = dccStorageConfig.isForceDownload();
    val md5Match = !bypassMd5 && fileExists && calcMd5Sum(absFile).equals(expectedMD5Sum); // Short circuit

    if (persist){
      val downloadFile = !fileExists
              || (fileExists && force)
              || (fileExists && !force && !bypassMd5 && !md5Match);
      if (downloadFile){
        return downloadFileByObjectId(objectId, absFilename);
      } else if (bypassMd5) {
        log.debug("File [{}] already exists but md5sum checking was disabled. Skipping download.", absFile);
        return absFile.toFile();
      } else {
        log.debug("File [{}] already exists and matches checksum. Skipping download.", absFile);
        return absFile.toFile();
      }
    } else {
      return downloadFileByObjectId(objectId, tempFile.toAbsolutePath().toString());
    }
  }

  @SneakyThrows
  private File downloadFileByObjectId(@NonNull final String objectId, @NonNull final String filename) {
    val objectUrl = getObjectUrl(objectId);
    val output = Paths.get(filename);

    @Cleanup
    val input = objectUrl.openStream();
    copy(input, output, REPLACE_EXISTING);

    return output.toFile();
  }

  @SneakyThrows
  private URL getObjectUrl(@NonNull final String objectId) {
    val storageUrl = new URL(dccStorageConfig.getUrl() + "/download/" + objectId + "?offset=0&length=-1&external=true");
    val connection = (HttpURLConnection) storageUrl.openConnection();
    connection.setRequestProperty(AUTHORIZATION, "Bearer " + dccStorageConfig.getToken());
    val object = readObject(connection);
    return getUrl(object);
  }

  public static SimpleDccStorageClient createSimpleDccStorageClient(DccStorageConfig dccStorageConfig) {
    return new SimpleDccStorageClient(dccStorageConfig);
  }

  private void checkForParentDir(@NonNull Path file) {
    Path absoluteFile = file;
    if (!file.isAbsolute()) {
      absoluteFile = file.toAbsolutePath();
    }
    checkState(absoluteFile.startsWith(outputDir),
        "The file [%s] must have the parent directory [%s] in its path",
        absoluteFile, outputDir);
  }

  public static String calcMd5Sum(@NonNull Path file) throws IOException {
    checkState(file.toFile().isFile(), "The input path [%s] is not a file", file);
    val bytes = Files.readAllBytes(file);
    return Hashing.md5()
        .newHasher()
        .putBytes(bytes)
        .hash()
        .toString();
  }

  @SneakyThrows
  private static URL getUrl(JsonNode object) {
    return new URL(object.get("parts").get(0).get("url").textValue());
  }

  @SneakyThrows
  private static JsonNode readObject(@NonNull final HttpURLConnection connection) {
    return DEFAULT.readTree(connection.getInputStream());
  }


  private static void initParentDir(@NonNull Path file) {
    val parentDir = file.getParent();
    initDir(parentDir);
  }

  @SneakyThrows
  private static void initDir(@NonNull final Path dir) {
    val dirDoesNotExist = !Files.exists(dir);
    if (dirDoesNotExist) {
      Files.createDirectories(dir);
    }
  }

  private static Path createTempFile(Path outputDir){
    val filename = "tmp." + System.currentTimeMillis();
    val path = outputDir.resolve(filename);
    path.toFile().deleteOnExit();
    return path;
  }

}
