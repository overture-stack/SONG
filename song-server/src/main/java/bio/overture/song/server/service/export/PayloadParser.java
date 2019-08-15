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

package bio.overture.song.server.service.export;

import static com.google.common.base.Preconditions.checkArgument;
import static lombok.AccessLevel.PRIVATE;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;

@Value
@RequiredArgsConstructor(access = PRIVATE)
public class PayloadParser {
  private static final String EXPERIMENT = "experiment";
  private static final String FILE = "file";
  private static final String SAMPLE = "sample";
  private static final String SPECIMEN = "specimen";
  private static final String DONOR = "donor";

  @NonNull private final JsonNode rootNode;
  @NonNull private final JsonNode experimentNode;
  @NonNull private final List<JsonNode> sampleNodes;
  @NonNull private final List<JsonNode> fileNodes;

  public List<JsonNode> getSpecimenNodes() {
    return sampleNodes.stream()
        .map(PayloadParser::getSpecimenNodeFromSample)
        .collect(toImmutableList());
  }

  public List<JsonNode> getDonorNodes() {
    return sampleNodes.stream()
        .map(PayloadParser::getDonorNodeFromSample)
        .collect(toImmutableList());
  }

  public static JsonNode readPath(JsonNode j, String fieldName) {
    checkField(j, fieldName);
    return j.path(fieldName);
  }

  public static PayloadParser createPayloadParser(JsonNode rootNode) {
    val experimentNode = getExperimentNode(rootNode);
    val fileNodes = getFileNodes(rootNode);
    val sampleNodes = getSampleNodes(rootNode);
    return new PayloadParser(rootNode, experimentNode, sampleNodes, fileNodes);
  }

  public static void checkField(JsonNode j, String fieldName) {
    checkArgument(
        j.has(fieldName), "The path '%s' does not contain the field '%s'", j.asText(), fieldName);
  }

  private static List<JsonNode> getSampleNodes(JsonNode rootNode) {
    return ImmutableList.copyOf(readPath(rootNode, SAMPLE).iterator());
  }

  private static List<JsonNode> getFileNodes(JsonNode rootNode) {
    return ImmutableList.copyOf(readPath(rootNode, FILE).iterator());
  }

  private static JsonNode getSpecimenNodeFromSample(JsonNode sampleNode) {
    return readPath(sampleNode, SPECIMEN);
  }

  private static JsonNode getDonorNodeFromSample(JsonNode sampleNode) {
    return readPath(sampleNode, DONOR);
  }

  private static JsonNode getExperimentNode(JsonNode rootNode) {
    return readPath(rootNode, EXPERIMENT);
  }
}
