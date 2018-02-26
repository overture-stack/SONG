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

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.icgc.dcc.song.server.model.entity.Study;
import org.icgc.dcc.song.server.model.entity.composites.StudyWithDonors;
import org.icgc.dcc.song.server.repository.StudyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class StudyService {

  @Autowired
  StudyRepository studyRepository;

  @Autowired
  StudyInfoService infoService;

  @Autowired
  DonorService donorService;

  @SneakyThrows
  public Study read(String studyId) {
    val study = studyRepository.read(studyId);
    if (study == null) {
      return study;
    }
    val info = infoService.read(studyId);
    study.setInfo(info);
    return study;
  }

  public boolean isStudyExist(String studyId){
    val study = studyRepository.read(studyId);
    return !isNull(study);
  }

  @SneakyThrows
  public StudyWithDonors readWithChildren(String studyId) {
    val study = new StudyWithDonors();
    val s = read(studyId);

    study.setStudy(s);
    study.setDonors(donorService.readByParentId(studyId));
    return study;
  }

  public int saveStudy(Study study) {
    val id = study.getStudyId();
    val status= studyRepository.create(id, study.getName(), study.getDescription(), study.getOrganization());
    infoService.create(id,study.getInfoAsString());
    return status;
  }

  public List<String> findAllStudies() {
    return studyRepository.findAllStudies();
  }

}
