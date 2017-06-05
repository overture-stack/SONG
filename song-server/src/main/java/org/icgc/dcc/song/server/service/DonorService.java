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

import static org.icgc.dcc.sodalite.server.model.enums.IdPrefix.Donor;

import java.util.List;

import org.icgc.dcc.sodalite.server.model.entity.Donor;
import org.icgc.dcc.sodalite.server.model.enums.IdPrefix;
import org.icgc.dcc.sodalite.server.repository.DonorRepository;
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

  public String create(Donor d) {
    val id = idService.generate(Donor);
    d.setDonorId(id);

    int status = donorRepository.create(d);
    if (status != 1) {
      return "error: Can't create" + d.toString();
    }
    d.getSpecimens().forEach(s -> specimenService.create(id, s));

    return "ok:" + id;
  }

  public Donor read(String id) {
    val donor = donorRepository.read(id);
    if (donor == null) {
      return null;
    }
    donor.setSpecimens(specimenService.readByParentId(id));
    return donor;
  }

  public List<Donor> readByParentId(String parentId) {
    val donors = donorRepository.readByParentId(parentId);
    donors.forEach(d -> d.setSpecimens(specimenService.readByParentId(d.getDonorId())));
    return donors;
  }

  public String update(Donor d) {
    if (donorRepository.update(d) == 1) {
      return "Updated";
    }
    return "Failed";
  }

  public String delete(String studyId, String id) {
    specimenService.deleteByParentId(id);
    donorRepository.delete(studyId, id);
    return "OK";
  }

  public String deleteByParentId(String studyId) {
    donorRepository.findByParentId(studyId).forEach(id -> delete(studyId, id));

    return "OK";
  }

  public String save(String studyId, Donor d) {
    d.setStudyId(studyId);

    String donorId = donorRepository.findByBusinessKey(studyId, d.getDonorSubmitterId());
    if (donorId == null) {
      donorId = idService.generate(IdPrefix.Donor);
      d.setDonorId(donorId);
      System.err.printf("Creating new donor with id=%s,gender='%s'\n", donorId, d.getDonorGender());
      donorRepository.create(d);
    } else {
      donorRepository.update(d);
    }
    return donorId;
  }

}