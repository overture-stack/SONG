package bio.overture.song.client;

import static bio.overture.song.client.cli.ClientMain.createClientMain;
import static bio.overture.song.core.utils.JsonDocUtils.getInputStreamClasspath;
import static java.lang.Boolean.parseBoolean;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.newInputStream;
import static java.util.Objects.isNull;

import bio.overture.song.client.config.Config;
import bio.overture.song.client.config.CustomRestClientConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class Main {

  private static final String DEFAULT_CONFIG_FILENAME = "application.yml";

  @SneakyThrows
  public static void main(String[] args) {
    val config = buildConfig(args[0]);
    val otherArgs = Arrays.stream(args).skip(1).toArray(String[]::new);
    createClientMain(config).run(otherArgs);
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
    val config = mapper.convertValue(tree, Config.class);
    updateWithEnv(config.getClient());
    return config;
  }

  private static void updateWithEnv(CustomRestClientConfig c) {
    c.setServerUrl(getString(c.getServerUrl(), "CLIENT_SERVER_URL"));
    c.setAccessToken(getString(c.getAccessToken(), "CLIENT_ACCESS_TOKEN"));
    c.setDebug(getBoolean(c.isDebug(), "CLIENT_DEBUG"));
    c.setProgramName(getString(c.getProgramName(), "CLIENT_PROGRAM_NAME"));
    c.setStudyId(getString(c.getStudyId(), "CLIENT_STUDY_ID"));
  }

  private static String getString(String value, String envVar) {
    val envValue = System.getenv(envVar);
    return isNull(envValue) ? value : envValue;
  }

  private static boolean getBoolean(boolean value, String envVar) {
    val envValue = System.getenv(envVar);
    return isNull(envValue) ? value : parseBoolean(envValue);
  }
}
