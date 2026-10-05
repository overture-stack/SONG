package bio.overture.song.server.utils;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.NonNull;
import lombok.val;
import org.springframework.jdbc.core.JdbcTemplate;

/** Reads Flyway's migration history in a form that can be compared between runs. */
public final class FlywayHistory {

  private FlywayHistory() {}

  /**
   * Returns one object per row of {@code flyway_schema_history}, in install order, with the columns
   * that identify a migration: version, description, type, script, checksum and success. Install
   * time, installer and execution time are left out because they differ on every run.
   */
  public static ArrayNode read(@NonNull JdbcTemplate jdbcTemplate) {
    val rows = JsonNodeFactory.instance.arrayNode();
    jdbcTemplate.query(
        "select version, description, type, script, checksum, success"
            + " from flyway_schema_history order by installed_rank",
        resultSet -> {
          val row = rows.addObject();
          row.put("version", resultSet.getString("version"));
          row.put("description", resultSet.getString("description"));
          row.put("type", resultSet.getString("type"));
          row.put("script", resultSet.getString("script"));
          val checksum = resultSet.getObject("checksum");
          if (checksum == null) {
            row.putNull("checksum");
          } else {
            row.put("checksum", ((Number) checksum).intValue());
          }
          row.put("success", resultSet.getBoolean("success"));
        });
    return rows;
  }
}
