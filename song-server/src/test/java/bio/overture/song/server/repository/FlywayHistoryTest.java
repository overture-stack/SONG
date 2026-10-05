package bio.overture.song.server.repository;

import static bio.overture.song.server.utils.contract.GoldenFiles.assertMatchesGolden;

import bio.overture.song.server.utils.FlywayHistory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Locks the Flyway history produced by migrating an empty database, with the golden file {@code
 * golden/db/flyway-history-empty-database}.
 *
 * <p>Version, description, type and checksum are what Flyway validates on startup against an
 * existing database, so any change to a migration's script, its Java class or the Flyway version
 * shows here. The Java migration V1_2 is recorded with type {@code SPRING_JDBC} by Flyway 5.
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class FlywayHistoryTest {

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  public void emptyDatabase_historyMatchesGolden() {
    assertMatchesGolden("db/flyway-history-empty-database", FlywayHistory.read(jdbcTemplate));
  }
}
