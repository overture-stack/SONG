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

package bio.overture.song.core.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import lombok.Data;
import lombok.val;

@Data
@JsonPropertyOrder({"studyId", "payloads"})
public class ExportedPayload {
  private String studyId;
  private List<JsonNode> payloads;

  public static ExportedPayload createExportedPayload(String studyId, List<JsonNode> payloads) {
    val p = new ExportedPayload();
    p.setStudyId(studyId);
    p.setPayloads(payloads);
    return p;
  }
}
