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
import org.icgc.dcc.song.server.model.entity.Donor;
import org.icgc.dcc.song.server.model.entity.composites.DonorWithSpecimens;
import org.icgc.dcc.song.server.repository.DonorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static org.icgc.dcc.song.core.exceptions.ServerErrors.DONOR_RECORD_FAILED;
import static org.icgc.dcc.song.core.exceptions.ServerException.checkServer;
import static org.icgc.dcc.song.core.utils.Responses.OK;

@RequiredArgsConstructor
@Service
public class DonorService {

  @Autowired
  private final DonorRepository donorRepository;
  @Autowired
  private final DonorInfoService infoService;
  @Autowired
  private final IdService idService;
  @Autowired
  private final SpecimenService specimenService;
  @Autowired
  private final StudyService studyService;

  public String create(@NonNull DonorWithSpecimens d) {
    studyService.checkStudyExist(d.getStudyId());
    val id = idService.generateDonorId(d.getDonorSubmitterId(), d.getStudyId());
    d.setDonorId(id);
    val donor = d.getDonor();

    val status = donorRepository.create(donor);
    checkServer(status == 1, this.getClass(),
        DONOR_RECORD_FAILED, "Cannot create Donor: %s", d.toString());
    infoService.create(id, donor.getInfoAsString());
    d.getSpecimens().forEach(s -> specimenService.create(d.getStudyId(), s));

    return id;
  }

  public Donor read(@NonNull String id) {
    val donor = donorRepository.read(id);
    if (donor == null) {
      return null;
    }
    donor.setInfo(infoService.readNullableInfo(id));
    return donor;
  }

  public DonorWithSpecimens readWithSpecimens(@NonNull String id) {
    val donor = new DonorWithSpecimens();
    donor.setDonor(read(id));

    donor.setSpecimens(specimenService.readByParentId(id));
    return donor;
  }

  public List<DonorWithSpecimens> readByParentId(@NonNull String parentId) {
    val donors = new ArrayList<DonorWithSpecimens>();
    val ids = donorRepository.findByParentId(parentId);
    ids.forEach(id -> donors.add(readWithSpecimens(id)));

    return donors;
  }

  public String update(@NonNull Donor donor) {
    if (donorRepository.update(donor) == 1) {
      infoService.update(donor.getDonorId(), donor.getInfoAsString());
      return "Updated";
    }
    return "Failed"; //TODO: [DCC-5644] need to properly handle this. Should an ServerException be thrown?
  }

  public String delete(@NonNull String studyId, @NonNull List<String> ids) {
    studyService.checkStudyExist(studyId);
    ids.forEach(x -> internalDelete(studyId, x));
    return OK;
  }

  public String delete(@NonNull String studyId, @NonNull String id) {
    studyService.checkStudyExist(studyId);
    return internalDelete(studyId, id);
  }

  private String internalDelete(@NonNull String studyId, @NonNull String id) {
    specimenService.deleteByParentId(id);
    donorRepository.delete(studyId, id);
    infoService.delete(id);
    return OK;
  }

  public String deleteByParentId(@NonNull String studyId) {
    donorRepository.findByParentId(studyId).forEach(id -> delete(studyId, id));
    return OK;
  }

  public String save(@NonNull String studyId, @NonNull Donor donor) {
    donor.setStudyId(studyId);

    String donorId = donorRepository.findByBusinessKey(studyId, donor.getDonorSubmitterId());
    if (donorId == null) {
      donorId = idService.generateDonorId(donor.getDonorSubmitterId(), studyId);
      donor.setDonorId(donorId);
      donorRepository.create(donor);
    } else {
      donorRepository.update(donor);
    }
    return donorId;
  }

}