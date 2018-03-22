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
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.server.model.entity.Sample;
import org.icgc.dcc.song.server.repository.SampleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Objects.isNull;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SAMPLE_ALREADY_EXISTS;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SAMPLE_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SAMPLE_ID_IS_CORRUPTED;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SAMPLE_REPOSITORY_CREATE_RECORD;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SAMPLE_REPOSITORY_DELETE_RECORD;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SAMPLE_REPOSITORY_UPDATE_RECORD;
import static org.icgc.dcc.song.core.exceptions.ServerException.checkServer;
import static org.icgc.dcc.song.core.utils.Responses.OK;

@Slf4j
@RequiredArgsConstructor
@Service
public class SampleService {

  @Autowired
  private final SampleRepository repository;

  @Autowired
  private final SampleInfoService infoService;

  @Autowired
  private final IdService idService;

  @Autowired
  private final StudyService studyService;

  private String createSampleId(String studyId, Sample sample){
    studyService.checkStudyExist(studyId);
    val inputSampleId = sample.getSampleId();
    val id = idService.generateSampleId(sample.getSampleSubmitterId(), studyId);
    checkServer(isNullOrEmpty(inputSampleId) || id.equals(inputSampleId), getClass(),
        SAMPLE_ID_IS_CORRUPTED,
        "The input sampleId '%s' is corrupted because it does not match the idServices sampleId '%s'",
        inputSampleId, id);
    checkSampleDoesNotExist(id);
    return id;
  }

  //TODO: [Related to SONG-260] should we add a specimenService.checkSpecimenExists(sample.getSpecimenId()) here?
  public String create(@NonNull String studyId, @NonNull Sample sample) {
    val id = createSampleId(studyId, sample);
    sample.setSampleId(id);
    sample.setSpecimenId(sample.getSpecimenId());
    int status = repository.create(sample);
    checkServer(status == 1,this.getClass(),
        SAMPLE_REPOSITORY_CREATE_RECORD, "Cannot create Sample: %s", sample.toString());
    infoService.create(id, sample.getInfoAsString());
    return id;
  }

  public Sample read(@NonNull String id) {
    val sample = repository.read(id);
    checkServer(!isNull(sample), getClass(), SAMPLE_DOES_NOT_EXIST,
        "The sample for sampleId '%s' could not be read because it does not exist", id);
    sample.setInfo(infoService.readNullableInfo(id));
    return sample;
  }

  List<Sample> readByParentId(@NonNull String parentId) {
    return repository.readByParentId(parentId);
  }

  public String update(@NonNull Sample sample) {
    checkSampleExists(sample.getSampleId());
    val status = repository.update(sample);
    checkServer(status == 1, getClass(), SAMPLE_REPOSITORY_UPDATE_RECORD,
        "Cannot update sampleId '%s' for sample '%s'", sample.getSampleId(), sample);
    infoService.update(sample.getSampleId(), sample.getInfoAsString());
    return OK;
  }

  public String delete(@NonNull String id) {
    checkSampleExists(id);
    val status = repository.delete(id);
    checkServer(status == 1, getClass(), SAMPLE_REPOSITORY_DELETE_RECORD,
        "Cannot delete sample with sampleId '%s'", id);
    infoService.delete(id);
    return OK;
  }

  public String delete(@NonNull List<String> ids){
    ids.forEach(this::delete);
    return OK;
  }

  String deleteByParentId(@NonNull String parentId) {
    val ids = repository.findByParentId(parentId);
    delete(ids);
    return OK;
  }

  public String findByBusinessKey(@NonNull String study, @NonNull String submitterId) {
    return repository.findByBusinessKey(study, submitterId);
  }

  public boolean isSampleExist(@NonNull String id){
    return !isNull(repository.read(id));
  }

  public void checkSampleExists(@NonNull String id){
    checkServer(isSampleExist(id), this.getClass(), SAMPLE_DOES_NOT_EXIST,
        "The sample with sampleId '%s' does not exist", id);
  }

  public void checkSampleDoesNotExist(@NonNull String id){
    checkServer(!isSampleExist(id), getClass(), SAMPLE_ALREADY_EXISTS,
        "The sample with sampleId '%s' already exists", id);
  }



}
