package bio.overture.song.server.repository;

import static bio.overture.song.server.repository.ReleaseDumpGenerator.DUMP_STUDY_ID;
import static bio.overture.song.server.utils.contract.GoldenFiles.assertMatchesGolden;
import static org.junit.jupiter.api.Assertions.assertEquals;

import bio.overture.song.server.contract.AbstractContractTest;
import bio.overture.song.server.utils.FlywayHistory;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Starts the current code against a database created by the release before the upgrade (see {@link
 * ReleaseDumpGenerator}), the way a deployment upgrades.
 *
 * <p>The Spring context only starts when Flyway validates the existing history, so a migration
 * whose checksum or type changed fails every test here. The tests then check the history gained no
 * rows and that the data written by the old release reads back with the same JSON.
 */
@TestPropertySource(properties = "test.data-initializr.enabled=false")
public class ReleaseDumpUpgradeTest extends AbstractContractTest {

  private static final PostgreSQLContainer<?> POSTGRES = ReleaseDatabase.startContainer();

  @DynamicPropertySource
  static void loadDumpAndRegisterDatasource(DynamicPropertyRegistry registry) {
    ReleaseDatabase.loadDump(POSTGRES);
    ReleaseDatabase.registerDatasource(registry, POSTGRES);
  }

  @AfterAll
  public static void stopDatabase() {
    POSTGRES.stop();
  }

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  public void flywayHistory_unchangedByStartup() {
    assertMatchesGolden("db/flyway-history-after-upgrade", FlywayHistory.read(jdbcTemplate));
  }

  @Test
  public void releaseData_readsBackWithSameJson() {
    val analysisIds =
        jdbcTemplate.queryForList(
            "select id from analysis where study_id = ?", String.class, DUMP_STUDY_ID);
    assertEquals(1, analysisIds.size());
    val analysisId = analysisIds.get(0);
    val normalizer = withAnalysisIds(createNormalizer(), "release", DUMP_STUDY_ID, analysisId);
    assertGolden(
        "db/upgrade-get-analysis",
        client.get("/studies/" + DUMP_STUDY_ID + "/analysis/" + analysisId),
        normalizer);
    assertGolden(
        "db/upgrade-list-schemas",
        client.get("/schemas?hideSchema=true&sort=version&sortOrder=ASC"),
        createNormalizer());
  }
}
