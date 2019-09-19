package bio.overture.song.client.util;

import bio.overture.song.client.cli.Status;
import lombok.NonNull;
import lombok.val;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileCheckers {

  public static Status checkPathExists(@NonNull Path path){
    val s = new Status();
    if (!Files.exists(path)){
      s.err("The path '%s' does not exist", path);
    }
    return s;
  }

  public static Status checkFileExists(@NonNull Path filepath){
    val s = checkPathExists(filepath);
    if (!s.hasErrors() && !Files.isRegularFile(filepath)){
      s.err("The path '%s' is not a file", filepath);
    }
    return s;
  }

  public static Status checkDirectoryExists(@NonNull Path dirpath){
    val s = checkPathExists(dirpath);
    if (!s.hasErrors() && !Files.isDirectory(dirpath)){
      s.err("The path '%s' is not a directory", dirpath);
    }
    return s;
  }

}
