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

import static org.icgc.dcc.song.server.model.enums.IdPrefix.Specimen;

import java.util.ArrayList;
import java.util.List;

import org.icgc.dcc.song.server.model.entity.Specimen;
import org.icgc.dcc.song.server.model.entity.composites.SpecimenSamples;
import org.icgc.dcc.song.server.model.enums.IdPrefix;
import org.icgc.dcc.song.server.repository.SpecimenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
@Service
public class SpecimenService {

  @Autowired
  private final IdService idService;
  @Autowired
  private final SpecimenRepository repository;
  @Autowired
  private final SampleService sampleService;

  public String create(String parentId, Specimen specimen) {
    val id = idService.generate(Specimen);
    specimen.setSpecimenId(id);
    specimen.setDonorId(parentId);
    int status = repository.create(specimen);
    if (status != 1) {
      return "error: Can't create" + specimen.toString();
    }

    return "ok:" + id;
  }

  public String createWithSamples(String parentId, SpecimenSamples specimen) {
    val status = create(parentId, specimen.getSpecimen());
    if (status.startsWith("error")) {
      return status;
    }
    specimen.getSamples().forEach(s -> sampleService.create(parentId, s));
    return status;
  }

  public Specimen read(String id) {
    val specimen = repository.read(id);
    if (specimen == null) {
      return null;
    }

    return specimen;
  }

  public SpecimenSamples readWithSamples(String id) {
    val specimen = repository.read(id);
    val s = new SpecimenSamples();
    s.setSpecimen(specimen);
    s.setSamples(sampleService.readByParentId(id));
    return s;
  }

  public List<SpecimenSamples> readByParentId(String parentId) {
    val ids = repository.findByParentId(parentId);
    val specimens = new ArrayList<SpecimenSamples>();
    ids.forEach(id -> specimens.add(readWithSamples(id)));
    return specimens;
  }

  public String update(Specimen s) {
    repository.update(s);
    return "ok";
  }

  public String delete(String id) {
    sampleService.deleteByParentId(id);
    repository.delete(id);
    return "ok";
  }

  public String deleteByParentId(String parentId) {
    repository.findByParentId(parentId).forEach(this::delete);
    return "ok";
  }

  public List<String> findByParentId(String donorId) {
    return repository.findByParentId(donorId);
  }

  public String findByBusinessKey(String studyId, String submitterId) {
    return repository.findByBusinessKey(studyId, submitterId);
  }

  public String save(String studyId, Specimen specimen) {
    String specimenId = repository.findByBusinessKey(studyId, specimen.getSpecimenSubmitterId());
    if (specimenId == null) {
      specimenId = idService.generate(IdPrefix.Specimen);
      specimen.setSpecimenId(specimenId);
      repository.create(specimen);
    } else {
      specimen.setSpecimenId(specimenId);
      repository.update(specimen);
    }
    return specimenId;
  }

}
