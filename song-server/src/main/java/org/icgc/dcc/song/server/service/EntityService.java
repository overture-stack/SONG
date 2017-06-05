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
import org.icgc.dcc.sodalite.server.model.entity.File;
import org.icgc.dcc.sodalite.server.model.entity.Sample;
import org.icgc.dcc.sodalite.server.model.entity.Specimen;
import org.icgc.dcc.sodalite.server.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
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
  DonorService donorService;
  @Autowired
  SpecimenService specimenService;
  @Autowired
  SampleService sampleService;
  @Autowired
  FileService fileService;

  @SneakyThrows
  public String saveDonor(String studyId, ObjectNode donor) {
    val d = JsonUtils.convertValue(donor, Donor.class);
    return donorService.save(studyId, d);

  }

  public String saveSpecimen(String studyId, String donorId, JsonNode specimen) {
    val s = JsonUtils.convertValue(specimen, Specimen.class);
    s.setDonorId(donorId);
    return specimenService.save(studyId, s);
  }

  public String saveSample(String studyId, String specimenId, JsonNode sample) {
    val s = JsonUtils.convertValue(sample, Sample.class);
    s.setSpecimenId(specimenId);
    return sampleService.save(studyId, s);
  }

  public String saveFile(String studyId, String sampleId, JsonNode file) {
    val f = JsonUtils.convertValue(file, File.class);
    f.setSampleId(sampleId);
    return fileService.save(studyId, f);
  }

}
