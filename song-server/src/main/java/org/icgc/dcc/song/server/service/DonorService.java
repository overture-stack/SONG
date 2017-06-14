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

import static org.icgc.dcc.song.server.model.enums.IdPrefix.Donor;

import java.util.List;

import lombok.NonNull;
import org.icgc.dcc.song.server.model.entity.Donor;
import org.icgc.dcc.song.server.model.enums.IdPrefix;
import org.icgc.dcc.song.server.repository.DonorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
@Service
public class DonorService {

  @Autowired
  DonorRepository donorRepository;
  @Autowired
  IdService idService;
  @Autowired
  SpecimenService specimenService;

  public String create(@NonNull Donor donor) {
    val id = idService.generate(Donor);
    donor.setDonorId(id);

    int status = donorRepository.create(donor);
    if (status != 1) {
      return "error: Can't create" + donor.toString();
    }
    donor.getSpecimens().forEach(s -> specimenService.create(id, s));

    return "ok:" + id;
  }

  public Donor read(@NonNull String id) {
    val donor = donorRepository.read(id);
    if (donor == null) {
      return null;
    }
    donor.setSpecimens(specimenService.readByParentId(id));
    return donor;
  }

  public List<Donor> readByParentId(@NonNull String parentId) {
    val donors = donorRepository.readByParentId(parentId);
    donors.forEach(d -> d.setSpecimens(specimenService.readByParentId(d.getDonorId())));
    return donors;
  }

  public String update(@NonNull Donor donor) {
    if (donorRepository.update(donor) == 1) {
      return "Updated";
    }
    return "Failed";
  }

  public String delete(@NonNull String studyId, @NonNull String id) {
    specimenService.deleteByParentId(id);
    donorRepository.delete(studyId, id);
    return "OK";
  }

  public String deleteByParentId(@NonNull String studyId) {
    donorRepository.findByParentId(studyId).forEach(id -> delete(studyId, id));

    return "OK";
  }

  public String save(@NonNull String studyId, @NonNull Donor donor) {
    donor.setStudyId(studyId);

    String donorId = donorRepository.findByBusinessKey(studyId, donor.getDonorSubmitterId());
    if (donorId == null) {
      donorId = idService.generate(IdPrefix.Donor);
      donor.setDonorId(donorId);
      System.err.printf("Creating new donor with id=%s,gender='%s'\n", donorId, donor.getDonorGender());
      donorRepository.create(donor);
    } else {
      donorRepository.update(donor);
    }
    return donorId;
  }

}