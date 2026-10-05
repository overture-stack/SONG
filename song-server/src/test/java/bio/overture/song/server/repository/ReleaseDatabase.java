package bio.overture.song.server.repository;

import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

/**
 * A Postgres container for tests that need direct control of the database, such as loading or
 * creating a dump, instead of the per-JVM {@code jdbc:tc:} database the {@code test} profile uses.
 */
public final class ReleaseDatabase {

  /** Same Postgres version as the {@code test} profile's {@code jdbc:tc:postgresql:14} URL. */
  private static final String POSTGRES_IMAGE = "postgres:14";

  /**
   * Dump of a database created by the release before the upgrade, with its data. Created by {@link
   * ReleaseDumpGenerator}.
   */
  public static final Path DUMP_PATH =
      Paths.get("src", "test", "resources", "db", "pre-upgrade-release-dump.sql");

  private static final String DUMP_PATH_IN_CONTAINER = "/tmp/pre-upgrade-release-dump.sql";

  private ReleaseDatabase() {}

  public static PostgreSQLContainer<?> startContainer() {
    final PostgreSQLContainer<?> container = new PostgreSQLContainer<>(POSTGRES_IMAGE);
    container.start();
    return container;
  }

  /**
   * Points the application datasource at {@code container}, with the {@code stringtype=unspecified}
   * parameter every Song JDBC URL needs for its Postgres enum columns.
   */
  public static void registerDatasource(
      @NonNull DynamicPropertyRegistry registry, @NonNull PostgreSQLContainer<?> container) {
    val jdbcUrl = container.getJdbcUrl();
    val separator = jdbcUrl.contains("?") ? "&" : "?";
    registry.add("spring.datasource.url", () -> jdbcUrl + separator + "stringtype=unspecified");
    registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    registry.add("spring.datasource.username", container::getUsername);
    registry.add("spring.datasource.password", container::getPassword);
  }

  /** Loads the release dump with psql; throws when psql reports an error. */
  @SneakyThrows
  public static void loadDump(@NonNull PostgreSQLContainer<?> container) {
    container.copyFileToContainer(
        MountableFile.forHostPath(DUMP_PATH.toAbsolutePath()), DUMP_PATH_IN_CONTAINER);
    val result =
        container.execInContainer(
            "psql",
            "-v",
            "ON_ERROR_STOP=1",
            "-U",
            container.getUsername(),
            "-d",
            container.getDatabaseName(),
            "-f",
            DUMP_PATH_IN_CONTAINER);
    if (result.getExitCode() != 0) {
      throw new IllegalStateException("Loading the release dump failed: " + result.getStderr());
    }
  }

  /** Returns a plain-SQL dump of the whole database, without owners or privileges. */
  @SneakyThrows
  public static String dump(@NonNull PostgreSQLContainer<?> container) {
    val result =
        container.execInContainer(
            "pg_dump",
            "--no-owner",
            "--no-privileges",
            "-U",
            container.getUsername(),
            container.getDatabaseName());
    if (result.getExitCode() != 0) {
      throw new IllegalStateException("pg_dump failed: " + result.getStderr());
    }
    return result.getStdout();
  }
}
