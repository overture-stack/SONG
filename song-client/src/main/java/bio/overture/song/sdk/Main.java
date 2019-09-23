package bio.overture.song.sdk;

import bio.overture.song.sdk.config.Config;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.Consumer;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.newInputStream;
import static java.util.Objects.isNull;
import static bio.overture.song.core.utils.JsonDocUtils.getInputStreamClasspath;

@Slf4j
public class Main {

  private static final String DEFAULT_CONFIG_FILENAME = "application.yml";
  public static Consumer<Integer> exit = System::exit;

  @SneakyThrows
  public static void main(String[] args) {
    val config = buildConfig(args[0]);
    val factory = new Factory(config);
    val otherArgs = Arrays.stream(args).skip(1).toArray(String[]::new);
    factory.buildClientMain().run(otherArgs);
  }

  @SneakyThrows
  public static Config buildConfig(String file) {
    InputStream is;
    if (isNull(file)) {
      is = getInputStreamClasspath(DEFAULT_CONFIG_FILENAME);
    } else if (exists(Paths.get(file))) {
      is = newInputStream(Paths.get(file));
    } else {
      is = getInputStreamClasspath(file);
    }
    val mapper = new ObjectMapper(new YAMLFactory());
    val tree = mapper.readTree(is);
    return mapper.convertValue(tree, Config.class);
  }
}
