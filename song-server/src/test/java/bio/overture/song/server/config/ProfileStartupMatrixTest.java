package bio.overture.song.server.config;

import static bio.overture.song.server.utils.contract.GoldenFiles.assertMatchesGolden;

import bio.overture.song.server.ServerMain;
import bio.overture.song.server.repository.ReleaseDatabase;
import bio.overture.song.server.utils.contract.RawHttpClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Starts the application under each supported profile combination and records whether it starts and
 * how it answers a few requests, with the golden file {@code golden/profiles/startup-matrix}.
 *
 * <p>The regular test suite only runs the {@code test} profile, so beans and settings that exist
 * only in these profiles (no-security filter chain, Score OAuth client, Kafka sender, Vault
 * bootstrap) are otherwise never loaded. Each combination gets its own application context on a
 * random port, against one shared Postgres container. The management port is also randomized so the
 * actuator can be probed; this overrides the {@code dev} profile's disabled management port.
 */
public class ProfileStartupMatrixTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  /** Each entry is passed to the application as its active profiles. */
  private static final List<String> PROFILE_COMBINATIONS =
      List.of(
          "default",
          "prod",
          "noSecurityDev",
          "score-client-cred",
          "kafka",
          "default,dev",
          "default,noSecurityDev",
          "default,score-client-cred",
          "default,kafka",
          "prod,kafka",
          "default,dev,secure",
          "prod,secure,kafka");

  private static PostgreSQLContainer<?> postgres;

  @BeforeAll
  public static void startDatabase() {
    postgres = ReleaseDatabase.startContainer();
  }

  @AfterAll
  public static void stopDatabase() {
    postgres.stop();
  }

  private static String[] applicationArguments() {
    val jdbcUrl = postgres.getJdbcUrl();
    val separator = jdbcUrl.contains("?") ? "&" : "?";
    return new String[] {
      "--server.port=0",
      "--management.server.port=0",
      "--spring.datasource.url=" + jdbcUrl + separator + "stringtype=unspecified",
      "--spring.datasource.driver-class-name=org.postgresql.Driver",
      "--spring.datasource.username=" + postgres.getUsername(),
      "--spring.datasource.password=" + postgres.getPassword()
    };
  }

  private static Throwable rootCause(Throwable throwable) {
    var cause = throwable;
    while (cause.getCause() != null && cause.getCause() != cause) {
      cause = cause.getCause();
    }
    return cause;
  }

  /** Starts one combination, records its responses, and always closes the context. */
  private static ObjectNode startAndProbe(String profiles) {
    val result = MAPPER.createObjectNode();
    ConfigurableApplicationContext context = null;
    try {
      context =
          new SpringApplicationBuilder(ServerMain.class)
              .profiles(profiles.split(","))
              .run(applicationArguments());
      result.put("startup", "started");

      val environment = context.getEnvironment();
      val client =
          new RawHttpClient("http://localhost:" + environment.getProperty("local.server.port"));
      result.put("GET /isAlive", client.get("/isAlive").getStatus());
      result.put("GET /studies/ABC123", client.get("/studies/ABC123").getStatus());
      val newStudyId = "PROFILE-" + profiles.replace(",", "-").toUpperCase();
      result.put(
          "POST /studies/{studyId}/",
          client
              .post(
                  "/studies/" + newStudyId + "/",
                  MAPPER.createObjectNode().put("studyId", newStudyId).toString())
              .getStatus());

      val managementPort = environment.getProperty("local.management.port");
      if (managementPort != null) {
        result.put(
            "GET /actuator/health",
            new RawHttpClient("http://localhost:" + managementPort)
                .get("/actuator/health")
                .getStatus());
      }
    } catch (Exception exception) {
      val cause = rootCause(exception);
      val message = String.valueOf(cause.getMessage()).split("\n")[0];
      result.put("startup", "failed");
      result.put("error", cause.getClass().getName() + ": " + message);
    } finally {
      if (context != null) {
        context.close();
      }
    }
    return result;
  }

  @Test
  public void eachProfileCombination_startupAndRequests() {
    val results = MAPPER.createObjectNode();
    for (val profiles : PROFILE_COMBINATIONS) {
      results.set(profiles, startAndProbe(profiles));
    }
    assertMatchesGolden("profiles/startup-matrix", results);
  }
}
