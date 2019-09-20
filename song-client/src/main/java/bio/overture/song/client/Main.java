package bio.overture.song.client;

import bio.overture.song.client.config.Config;
import bio.overture.song.client.config.Factory;
import bio.overture.song.core.utils.JsonDocUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.Consumer;

@Slf4j
public class Main {

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
    //    val is = JsonDocUtils.getInputStreamClasspath();
    InputStream is;
    if (Files.exists(Paths.get(file))) {
      is = Files.newInputStream(Paths.get(file));
    } else {
      is = JsonDocUtils.getInputStreamClasspath(file);
    }
    val mapper = new ObjectMapper(new YAMLFactory());
    val tree = mapper.readTree(is);
    return mapper.convertValue(tree, Config.class);
  }
}
