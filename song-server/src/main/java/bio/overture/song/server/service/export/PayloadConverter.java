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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;

import static org.icgc.dcc.common.core.util.stream.Streams.stream;
import static bio.overture.song.server.service.export.PayloadParser.checkField;
import static bio.overture.song.server.service.export.PayloadParser.readPath;

@RequiredArgsConstructor
public class PayloadConverter {
  private static final String INFO = "info";
  private static final String ANALYSIS_ID = "analysisId";
  private static final String STUDY_ID = "studyId";
  private static final String OBJECT_ID = "objectId";
  private static final String SAMPLE_ID = "sampleId";
  private static final String SPECIMEN_ID = "specimenId";
  private static final String DONOR_ID = "donorId";
  private static final String ANALYSIS_STATE= "analysisState";

  private final boolean includeAnalysisId;

  public JsonNode convert(PayloadParser parser){
    if(!includeAnalysisId){
      removeAnalysisId(parser);
    }

    removeAnalysisState(parser);
    removeExperimentFields(parser);
    removeSamplesFields(parser);
    removeDonorFields(parser);
    removeSpecimenFields(parser);
    removeFilesFields(parser);

    removeEmptyInfoFields(parser);
    return parser.getRootNode();
  }

  public static PayloadConverter createPayloadConverter(boolean includeAnalysisId) {
    return new PayloadConverter(includeAnalysisId);
  }

  private static void removeEmptyInfoFields(PayloadParser parser){
    val list = Lists.<JsonNode>newArrayList();
    list.add(parser.getRootNode());
    list.add(parser.getExperimentNode());
    list.addAll(parser.getDonorNodes());
    list.addAll(parser.getSampleNodes());
    list.addAll(parser.getFileNodes());
    list.addAll(parser.getSpecimenNodes());
    removeInfoIfEmpty(list);
  }

  private static void removeAnalysisId(PayloadParser parser){
    removePath(parser.getRootNode(), ANALYSIS_ID);
  }

  private static void removeAnalysisState(PayloadParser parser){
    removePath(parser.getRootNode(), ANALYSIS_STATE);
  }

  private static void removeExperimentFields(PayloadParser parser){
    removePath(parser.getExperimentNode(), ANALYSIS_ID);
  }


  private static void removeFilesFields(PayloadParser parser){
    parser.getFileNodes()
        .forEach(fileNode -> removePath(fileNode, ANALYSIS_ID, STUDY_ID, OBJECT_ID));
  }

  private static void removeSamplesFields(PayloadParser parser){
    parser.getSampleNodes()
        .forEach(sampleNode -> removePath(sampleNode, SAMPLE_ID, SPECIMEN_ID));
  }

  private static void removeSpecimenFields(PayloadParser parser){
    parser.getSpecimenNodes()
        .forEach(specimenNode -> removePath(specimenNode, DONOR_ID, SPECIMEN_ID));
  }

  private static void removeDonorFields(PayloadParser parser){
    parser.getDonorNodes()
        .forEach(donorNode -> removePath(donorNode, DONOR_ID, STUDY_ID) );
  }

  private static JsonNode removePath(JsonNode j, String ... fieldNames){
    stream(fieldNames).forEach(x -> {
      checkField(j, x);
      ((ObjectNode)j).remove(x);
    });
    return j;
  }

  private static void removeInfoIfEmpty(List<JsonNode> nodes){
    nodes.forEach(j -> {
      JsonNode infoPath = readPath(j, INFO);
      if (infoPath.isNull() || Iterators.size(infoPath.fieldNames()) == 0){
        ((ObjectNode)j).remove(INFO);
      }
    });
  }

}
