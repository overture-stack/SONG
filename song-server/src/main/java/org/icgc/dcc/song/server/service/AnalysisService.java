/*
 * Copyright (c) 2017 The Ontario Institute for Cancer Research. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.icgc.dcc.song.server.service;

import static java.lang.String.format;
import static org.icgc.dcc.song.server.model.enums.Constants.ANALYSIS_TYPE;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.model.enums.IdPrefix;
import org.icgc.dcc.song.server.repository.AnalysisRepository;
import org.icgc.dcc.song.server.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {

  @Autowired
  private final AnalysisRepository repository;
  @Autowired
  private final IdService idService;
  @Autowired
  private final EntityService entityService;

  @SneakyThrows
  public String getAnalysisType(String json) {
    return getAnalysisType(JsonUtils.readTree(json));
  }

  public String getAnalysisType(JsonNode node) {
    for (val type : ANALYSIS_TYPE) {
      log.info("Checking analysis type " + type);
      if (node.has(type)) {
        return type;
      }
    }
    return null;
  }

  void createAnalysis(String id, String studyId, String type) {
    repository.createAnalysis(id, studyId, type);
  }

  @SneakyThrows
  public String create(String studyId, String json) {
    val node = JsonUtils.readTree(json);
    val type = getAnalysisType(node);

    val id = idService.generate(IdPrefix.Analysis);

    createAnalysis(id, studyId, type);

    val study = node.get("study");
    val fileIds = saveStudy(studyId, study);

    for (val f : fileIds) {
      addFile(id, f);
    }

    val analysis = node.get(type.toString());
    switch (type) {
    case "sequencingRead":
      return createSequencingRead(id, analysis);
    case "variantCall":
      return createVariantCall(id, analysis);
    default:
      return "Upload Analysis failed: Unknown Analysis Type";
    }
  }

  void addFile(String id, String fileId) {
    repository.addFile(id, fileId);
  }

  ObjectNode get(JsonNode root, String key) {
    val node = root.get(key);
    if (node.isObject()) {
      return (ObjectNode) node;
    }
    throw new IllegalArgumentException(format("node '%s'{%s} is not an object node", node, key));
  }

  @SneakyThrows
  Collection<String> saveStudy(String studyId, JsonNode study) {
    val fileIds = new HashSet<String>();

    val donor = get(study, "donor");
    val specimen = donor.get("specimen");
    val sample = specimen.get("sample");
    val files = sample.get("files");

    donor.put("studyId", studyId);
    donor.remove("specimen");
    val donorId = entityService.saveDonor(studyId, donor);

    val specimenId = entityService.saveSpecimen(studyId, donorId, specimen);

    val sampleId = entityService.saveSample(studyId, specimenId, sample);

    for (val file : files) {
      val fileId = entityService.saveFile(studyId, sampleId, file);
      fileIds.add(fileId);
    }

    return fileIds;
  }

  String createSequencingRead(String id, JsonNode node) {
    val strategy = node.get("libraryStrategy").asText();
    val isPaired = node.get("pairedEnd").asBoolean();
    val size = node.get("insertSize").asLong();
    val isAligned = node.get("aligned").asBoolean();
    val tool = node.get("alignmentTool").asText();
    val genome = node.get("referenceGenome").asText();

    repository.createSequencingRead(id, strategy, isPaired, size, isAligned, tool, genome);

    return id;
  }

  String createVariantCall(String id, JsonNode node) {
    val tool = node.get("variantCallingTool").asText();
    val tumorId = node.get("tumourSampleSubmitterId").asText();
    val normalId = node.get("matchedNormalSampleSubmitterId").asText();
    repository.createVariantCall(id, tool, tumorId, normalId);
    return id;
  }

  public List<String> getAnalyses(Map<String, String> params) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getAnalysisById(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  public String updateAnalysis(String studyId, String json) {
    // TODO Auto-generated method stub
    return null;
  }

  public List<File> readFilesByAnalysisId(String id) {
    return repository.getFilesById(id);
  }

  public String publish(String id) {
    val files = readFilesByAnalysisId(id);
    List<String> missingUploads=new ArrayList<>();
    for (val f: files) {
       if ( !confirmUploaded(f.getObjectId()) ) {
         missingUploads.add(f.getObjectId());
       }
    }
    if (missingUploads.isEmpty()) {
      repository.updateState(id,"PUBLISHED");
      return JsonUtils.fromSingleQuoted(format("'status':'success','msg': 'Analysis %s' successfully published.'", id));
    }
    return JsonUtils.fromSingleQuoted(format("'status': 'failure', 'msg': 'The following file ids must be published before analysis id %s can be published: %s',id, files"));
  }

  public boolean confirmUploaded(String fileId) {
    // TODO: Close the loop; make this actually look up the real status from the storage server.
    return true;
  }

}
