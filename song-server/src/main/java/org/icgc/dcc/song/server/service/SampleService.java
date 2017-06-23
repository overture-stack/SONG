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
import org.icgc.dcc.song.server.model.entity.Sample;
import org.icgc.dcc.song.server.model.enums.IdPrefix;
import org.icgc.dcc.song.server.repository.SampleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.icgc.dcc.song.core.exceptions.ServerErrors.SAMPLE_RECORD_FAILED;
import static org.icgc.dcc.song.core.exceptions.ServerException.buildServerException;
import static org.icgc.dcc.song.core.utils.Responses.OK;
import static org.icgc.dcc.song.server.model.enums.IdPrefix.Sample;

@RequiredArgsConstructor
@Service
public class SampleService {
  private static final String MESSAGE_CONTEXT = SampleService.class.getSimpleName();

  @Autowired
  SampleRepository repository;
  @Autowired
  IdService idService;
  @Autowired
  FileService fileService;

  public String create(@NonNull String parentId, @NonNull Sample sample) {
    val id = idService.generate(Sample);
    sample.setSampleId(id);
    sample.setSpecimenId(parentId);
    int status = repository.create(sample);

    if (status != 1) {
      throw buildServerException(MESSAGE_CONTEXT, SAMPLE_RECORD_FAILED, "Cannot create Sample: %s", sample.toString());
    }

    return id;
  }

  public Sample read(@NonNull String id) {
    val sample = repository.read(id);
    if (sample == null) {
      return null;
    }
    return sample;
  }

  public List<Sample> readByParentId(@NonNull String parentId) {
    val samples = repository.readByParentId(parentId);
    return samples;
  }

  public String update(@NonNull Sample sample) {
    repository.update(sample);
    return OK;
  }

  public String delete(@NonNull String id) {
    repository.delete(id);
    return OK;
  }

  public String deleteByParentId(@NonNull String parentId) {
    val ids = repository.findByParentId(parentId);
    ids.forEach(this::delete);

    return OK;
  }

  public List<String> findByParentId(@NonNull String specimenId) {
    return repository.findByParentId(specimenId);
  }

  public String findByBusinessKey(@NonNull String study, @NonNull String submitterId) {
    return repository.findByBusinessKey(study, submitterId);
  }

  public String save(@NonNull String studyId, @NonNull Sample sample) {
    String sampleId = repository.findByBusinessKey(studyId, sample.getSampleSubmitterId());
    if (sampleId == null) {
      sampleId = idService.generate(IdPrefix.Sample);
      sample.setSampleId(sampleId);
      repository.create(sample);
    } else {
      sample.setSampleId(sampleId);
      repository.update(sample);
    }
    return sampleId;
  }

}
