/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.icgc.dcc.song.server.repository;

import lombok.val;
import org.icgc.dcc.song.server.model.entity.Donor;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;

public interface DonorRepository extends JpaRepository<Donor, String> {

  default  int create( Donor donor){
    save(donor);
    return 1;
  }

  default Donor read( String donorId){
    return findById(donorId).orElse(null);
  }

  default int update( Donor donor){
    save(donor);
    return 1;
  }

  default int delete( String studyId,  String id){
    val req = DonorRequest.create(id, studyId, null, null);
    val results = findAll(Example.of(req));
    deleteAll(results);
    return results.size();
  }

  default List<String> findByParentId( String parentId){
    val req = DonorRequest.create(null, parentId, null, null);
    return findAll(Example.of(req)).stream()
        .map(Donor::getDonorId)
        .collect(toImmutableList());
  }

  default String findByBusinessKey( String studyId,  String key){
    val req = DonorRequest.create(null, studyId, key, null);
    val results = findAll(Example.of(req));
    checkState(results.size() < 2,
        "There cannot be more than 2 results for studyId '{}' and key '{}'",
        studyId, key);
    if (results.isEmpty()){
      return null;
    }
    return results.get(0).getDonorId();
  }

  class DonorRequest extends  Donor {
    private String gender;

    @Override
    public void setDonorGender(String gender) {
      this.gender =  gender;
    }

    public String getDonorGender() {
      return gender;
    }

    public static DonorRequest create(String id, String studyId, String submitterId, String gender){
      val d = new DonorRequest();
      d.setDonorGender(gender);
      d.setDonorId(id);
      d.setStudyId(studyId);
      d.setDonorSubmitterId(submitterId);
      return d;
    }

  }

}

