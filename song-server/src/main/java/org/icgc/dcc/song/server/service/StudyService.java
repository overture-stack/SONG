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

import java.util.List;

import lombok.val;
import org.icgc.dcc.song.server.model.entity.Study;
import org.icgc.dcc.song.server.model.entity.composites.StudyDonors;
import org.icgc.dcc.song.server.repository.StudyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Service
@RequiredArgsConstructor
public class StudyService {

  /**
   * Dependencies
   */
  @Autowired
  StudyRepository studyRepository;
  @Autowired
  DonorService donorService;

  @SneakyThrows
  public Study read(String studyId) {
    return studyRepository.read(studyId);
  }

  @SneakyThrows
  public StudyDonors readWithChildren(String studyId) {
    val study = new StudyDonors();
    study.setStudy(read(studyId));
    study.setDonors(donorService.readByParentId(studyId));
    return study;
  }

  @SneakyThrows
  public List<Study> readByName(String name) {
    return studyRepository.readByName(name);
  }

  public int saveStudy(Study study) {
    return studyRepository.create(study.getStudyId(), study.getName(), study.getDescription(), study.getOrganization());
  }

}
