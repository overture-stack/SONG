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
import lombok.SneakyThrows;
import lombok.val;
import org.icgc.dcc.song.server.model.entity.Study;
import org.icgc.dcc.song.server.repository.StudyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.lang.Thread.currentThread;
import static java.util.Objects.isNull;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.STUDY_ALREADY_EXISTS;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.STUDY_ID_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.exceptions.ServerException.checkServer;

@Service
@RequiredArgsConstructor
public class StudyService {

  @Autowired
  StudyRepository studyRepository;

  @Autowired
  StudyInfoService infoService;

  @SneakyThrows
  public Study read(String studyId) {
    val study = studyRepository.read(studyId);
    checkServer(!isNull(study), getClass(), STUDY_ID_DOES_NOT_EXIST,
        "The studyId '%s' does not exist", studyId);
    val info = infoService.readNullableInfo(studyId);
    study.setInfo(info);
    return study;
  }

  public boolean isStudyExist(String studyId){
    val study = studyRepository.read(studyId);
    return !isNull(study);
  }

  public int saveStudy(Study study) {
    val id = study.getStudyId();
    checkServer(!isStudyExist(id), getClass(), STUDY_ALREADY_EXISTS,
        "The studyId '%s' already exists. Cannot save the study: %s " ,
        id,study);
    val status= studyRepository.create(id, study.getName(), study.getDescription(), study.getOrganization());
    infoService.create(id,study.getInfoAsString());
    return status;
  }

  public List<String> findAllStudies() {
    return studyRepository.findAllStudies();
  }

  @SneakyThrows
  public void checkStudyExist(@NonNull String studyId){
    val previousCallingClass = Class.forName(currentThread().getStackTrace()[2].getClassName());
    checkServer(isStudyExist(studyId), previousCallingClass, STUDY_ID_DOES_NOT_EXIST,
        "The studyId '%s' does not exist", studyId);
  }

}
