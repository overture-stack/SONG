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

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.icgc.dcc.song.server.model.entity.Specimen;
import org.icgc.dcc.song.server.model.entity.composites.SpecimenWithSamples;
import org.icgc.dcc.song.server.model.enums.IdPrefix;
import org.icgc.dcc.song.server.repository.SpecimenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static org.icgc.dcc.song.core.exceptions.ServerErrors.SPECIMEN_RECORD_FAILED;
import static org.icgc.dcc.song.core.exceptions.ServerException.buildServerException;
import static org.icgc.dcc.song.core.utils.Responses.OK;

@RequiredArgsConstructor
@Service
public class SpecimenService {

  @Autowired
  private final IdService idService;
  @Autowired
  private final SampleService sampleService;
  @Autowired
  private final SpecimenInfoService infoService;
  @Autowired
  private final SpecimenRepository repository;


  public String create(@NonNull String studyId, @NonNull Specimen specimen) {
    val id = idService.generateSpecimenId(studyId, specimen.getSpecimenSubmitterId());
    specimen.setSpecimenId(id);
    int status = repository.create(specimen);
    if (status != 1) {
      throw buildServerException(this.getClass(), SPECIMEN_RECORD_FAILED,
          "Cannot create Specimen: %s", specimen.toString());
    }
    infoService.create(id, specimen.getInfo());

    return id;
  }

  public Specimen read(@NonNull String id) {
    val specimen = repository.read(id);
    if (specimen == null) {
      return null;
    }
    specimen.setInfo(infoService.read(id));

    return specimen;
  }

  public SpecimenWithSamples readWithSamples(String id) {
    val specimen = read(id);
    val s = new SpecimenWithSamples();
    s.setSpecimen(specimen);
    s.setSamples(sampleService.readByParentId(id));
    return s;
  }

  public List<SpecimenWithSamples> readByParentId(String parentId) {
    val ids = repository.findByParentId(parentId);
    val specimens = new ArrayList<SpecimenWithSamples>();
    ids.forEach(id -> specimens.add(readWithSamples(id)));

    return specimens;
  }

  public String update(@NonNull Specimen specimen) {
    repository.update(specimen);
    infoService.update(specimen.getSpecimenId(),specimen.getInfo());
    return OK;
  }

  public String delete(@NonNull String id) {
    sampleService.deleteByParentId(id);
    repository.delete(id);
    infoService.delete(id);
    return OK;
  }

  public String deleteByParentId(@NonNull String parentId) {
    repository.findByParentId(parentId).forEach(this::delete);
    return OK;
  }

  public List<String> findByParentId(@NonNull String donorId) {
    return repository.findByParentId(donorId);
  }

  public String findByBusinessKey(@NonNull String studyId, @NonNull String submitterId) {
    return repository.findByBusinessKey(studyId, submitterId);
  }

  public String save(@NonNull String studyId, @NonNull Specimen specimen) {
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
