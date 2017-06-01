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
 */
package org.icgc.dcc.sodalite.server.service;

import org.icgc.dcc.sodalite.server.model.entity.Donor;
import org.icgc.dcc.sodalite.server.model.entity.Specimen;
import org.icgc.dcc.sodalite.server.model.utils.IdPrefix;
import org.icgc.dcc.sodalite.server.repository.DonorRepository;
import org.icgc.dcc.sodalite.server.repository.FileRepository;
import org.icgc.dcc.sodalite.server.repository.SampleRepository;
import org.icgc.dcc.sodalite.server.repository.SpecimenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@Service
@AllArgsConstructor
/***
 * Save JSON objects into repository storage
 */

public class EntityService {

  @Autowired
  IdService idService;
  @Autowired
  DonorRepository donorRepository;
  @Autowired
  SpecimenRepository specimenRepository;
  @Autowired
  SampleRepository sampleRepository;
  @Autowired
  FileRepository fileRepository;

  private void setDefault(ObjectNode node, String field, String value) {
    if (node.has(field)) {
      return;
    }
    node.put(field, value);
  }

  @SneakyThrows
  public String saveDonor(String studyId, ObjectNode donor) {
    val mapper = new ObjectMapper();
    mapper.disable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES);
    mapper.disable(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS);
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    boolean hasSpecimen = false;
    JsonNode specimen = null;
    setDefault(donor, "donorId", "");
    setDefault(donor, "studyId", "");
    setDefaultObject(donor, "donorInfo");
    if (donor.has("specimen")) {
      specimen = donor.get("specimen");
      hasSpecimen = true;
      donor.remove("specimen");
    }

    val d = mapper.convertValue(donor, Donor.class);
    if (hasSpecimen) {
      donor.put("specimen", specimen);

      if (specimen.isObject()) {
        val spNode = (ObjectNode) specimen;
        setDefault(spNode, "specimenId", "");
        setDefault(spNode, "donorId", "");

        val sp = mapper.convertValue(spNode, Specimen.class);
        d.addSpecimen(sp);
      }
    }
    d.setStudyId(studyId);

    String donorId = donorRepository.getIdByBusinessKey(studyId, d.getDonorSubmitterId());
    if (donorId == null) {
      donorId = idService.generate(IdPrefix.Donor);
      d.setDonorId(donorId);
      System.err.printf("Creating new donor with id=%s,gender='%s'\n", donorId, d.getDonorGender());
      donorRepository.create(d);
    } else {
      donorRepository.update(d);
    }
    return donorId;
  }

  private void setDefaultObject(ObjectNode donor, String key) {
    if (donor.has(key)) {
      return;
    }
    donor.with(key);
  }

  private String getDefault(JsonNode node, String key, String defaultValue) {
    if (node == null) {
      return defaultValue;
    }
    if (node.has(key)) {
      return node.get(key).asText();
    }
    return defaultValue;
  }

  public String saveSpecimen(String studyId, String donorId, JsonNode specimen) {
    val submitterId = getDefault(specimen, "specimenSubmitterId", "");
    val class_ = getDefault(specimen, "specimenClass", "");
    val type = getDefault(specimen, "specimenType", "");

    String specimenId = specimenRepository.getIdByBusinessKey(studyId, submitterId);
    if (specimenId == null) {
      specimenId = idService.generate(IdPrefix.Specimen);
      specimenRepository.save(specimenId, donorId, submitterId, class_, type);
    } else {
      specimenRepository.set(specimenId, submitterId, class_, type);
    }
    return specimenId;
  }

  public String saveSample(String studyId, String specimenId, JsonNode sample) {
    val submitterId = sample.get("sampleSubmitterId").asText();
    val type = sample.get("sampleType").asText();

    String sampleId = sampleRepository.getIdByBusinessKey(studyId, submitterId);
    if (sampleId == null) {
      sampleId = idService.generate(IdPrefix.Sample);
      sampleRepository.save(sampleId, specimenId, submitterId, type);
    } else {
      sampleRepository.set(sampleId, submitterId, type);
    }
    return sampleId;
  }

  public String saveFile(String studyId, String sampleId, JsonNode file) {
    val name = file.get("fileName").asText();
    val size = file.get("fileSize").asLong();
    val type = file.get("fileType").asText();
    val md5 = file.get("fileMd5").asText();

    String metadata = "";
    if (file.has("fileMetadata")) {
      metadata = file.get("fileMetadata").asText();
    }

    String fileId = fileRepository.getIdByBusinessKey(studyId, name);
    if (fileId == null) {
      fileId = idService.generate(IdPrefix.File);
      fileRepository.save(fileId, sampleId, name, size, type, md5, metadata);
    } else {
      fileRepository.set(fileId, name, size, type, md5, metadata);
    }
    return fileId;
  }

}
