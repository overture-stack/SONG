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

import static org.icgc.dcc.sodalite.server.model.enums.IdPrefix.Sample;

import java.util.List;

import org.icgc.dcc.sodalite.server.model.entity.Sample;
import org.icgc.dcc.sodalite.server.model.enums.IdPrefix;
import org.icgc.dcc.sodalite.server.repository.SampleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
@Service
public class SampleService {

  @Autowired
  SampleRepository repository;
  @Autowired
  IdService idService;
  @Autowired
  FileService fileService;

  public String create(String parentId, Sample s) {
    val id = idService.generate(Sample);
    s.setSampleId(id);
    s.setSpecimenId(parentId);
    int status = repository.create(s);

    if (status != 1) {
      return "error: Can't create" + s.toString();
    }
    s.getFiles().forEach(f -> fileService.create(id, f));
    return "ok:" + id;
  }

  public Sample read(String id) {
    val sample = repository.read(id);
    if (sample == null) {
      return null;
    }
    sample.setFiles(fileService.readByParentId(id));
    return sample;
  }

  public List<Sample> readByParentId(String parentId) {
    val samples = repository.readByParentId(parentId);
    samples.forEach(s -> s.setFiles(fileService.readByParentId(s.getSampleId())));
    return samples;
  }

  public String update(Sample s) {
    repository.update(s);
    return "ok";
  }

  public String delete(String id) {
    repository.delete(id);
    return "ok";
  }

  public String deleteByParentId(String parentId) {
    val ids = repository.findByParentId(parentId);
    ids.forEach(this::delete);

    return "ok";
  }

  public List<String> findByParentId(String specimenId) {
    return repository.findByParentId(specimenId);
  }

  public String findByBusinessKey(String study, String submitterId) {
    return repository.findByBusinessKey(study, submitterId);
  }

  public String save(String studyId, Sample s) {
    String sampleId = repository.findByBusinessKey(studyId, s.getSampleSubmitterId());
    if (sampleId == null) {
      sampleId = idService.generate(IdPrefix.Sample);
      s.setSampleId(sampleId);
      repository.create(s);
    } else {
      s.setSampleId(sampleId);
      repository.update(s);
    }
    return sampleId;
  }

}
