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
package bio.overture.song.server.service;

import bio.overture.song.server.model.entity.Donor;
import bio.overture.song.server.model.entity.composites.DonorWithSpecimens;
import bio.overture.song.server.repository.DonorRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static bio.overture.song.core.exceptions.ServerErrors.DONOR_ALREADY_EXISTS;
import static bio.overture.song.core.exceptions.ServerErrors.DONOR_DOES_NOT_EXIST;
import static bio.overture.song.core.exceptions.ServerErrors.DONOR_ID_IS_CORRUPTED;
import static bio.overture.song.core.exceptions.ServerErrors.ENTITY_NOT_RELATED_TO_STUDY;
import static bio.overture.song.core.exceptions.ServerException.buildServerException;
import static bio.overture.song.core.exceptions.ServerException.checkServer;
import static bio.overture.song.core.utils.Responses.OK;

@RequiredArgsConstructor
@Service
@Transactional
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

  private String createDonorId(DonorWithSpecimens donorWithSpecimens){
    studyService.checkStudyExist(donorWithSpecimens.getStudyId());
    val inputDonorId = donorWithSpecimens.getDonorId();
    val id = idService.generateDonorId(donorWithSpecimens.getDonorSubmitterId(), donorWithSpecimens.getStudyId());
    checkServer(isNullOrEmpty(inputDonorId) || id.equals(inputDonorId), getClass(),
        DONOR_ID_IS_CORRUPTED,
        "The input donorId '%s' is corrupted because it does not match the idServices donorId '%s'",
        inputDonorId, id);
    checkDonorDoesNotExist(id);
    return id;
  }

  public String create(@NonNull DonorWithSpecimens donorWithSpecimens) {
    val id = createDonorId(donorWithSpecimens);
    donorWithSpecimens.setDonorId(id);

    /**
     * Cannot pass DonorWithSpecimen object to donorReposityr.create() method
     */
    val donor = donorWithSpecimens.createDonor();
    donorRepository.save(donor);
    infoService.create(id, donor.getInfoAsString());
    donorWithSpecimens.getSpecimens().forEach(s -> specimenService.create(donorWithSpecimens.getStudyId(), s));
    return id;
  }

  public void checkDonorAndStudyRelated(@NonNull String studyId, @NonNull String id){
    val numDonors = donorRepository.countAllByStudyIdAndDonorId(studyId, id);
    if (numDonors < 1){
      studyService.checkStudyExist(studyId);
      val donor = unsecuredRead(id);
      throw buildServerException(getClass(), ENTITY_NOT_RELATED_TO_STUDY,
          "The donorId '%s' is not related to the input studyId '%s'. It is actually related to studyId '%s'",
          id, studyId, donor.getStudyId() );
    }
  }

  public Donor securedRead(@NonNull String studyId, String id) {
    checkDonorAndStudyRelated(studyId, id);
    return unsecuredRead(id);
  }

  public Donor unsecuredRead(@NonNull String id) {
    val donorResult = donorRepository.findById(id);
    checkServer(donorResult.isPresent(), getClass(), DONOR_DOES_NOT_EXIST,
      "The donor for donorId '%s' could not be read because it does not exist", id);
    val donor = donorResult.get();
    donor.setInfo(infoService.readNullableInfo(id));
    return donor;
  }

  public DonorWithSpecimens readWithSpecimens(@NonNull String id) {
    val donor = new DonorWithSpecimens();
    donor.setDonor(unsecuredRead(id));

    donor.setSpecimens(specimenService.readByParentId(id));
    return donor;
  }

  public List<DonorWithSpecimens> readByParentId(@NonNull String parentId) {
    studyService.checkStudyExist(parentId);
    val donors = new ArrayList<DonorWithSpecimens>();
    val ids = donorRepository.findAllByStudyId(parentId).stream()
        .map(Donor::getDonorId)
        .collect(toImmutableList());
    ids.forEach(id -> donors.add(readWithSpecimens(id)));
    return donors;
  }

  public boolean isDonorExist(@NonNull String id){
    return donorRepository.existsById(id);
  }

  public void checkDonorExists(@NonNull String id){
    checkServer(isDonorExist(id), this.getClass(), DONOR_DOES_NOT_EXIST,
        "The donor with donorId '%s' does not exist", id);
  }

  public void checkDonorDoesNotExist(@NonNull String id){
    checkServer(!isDonorExist(id), getClass(), DONOR_ALREADY_EXISTS,
        "The donor with donorId '%s' already exists", id);
  }

  public String update(@NonNull Donor donorUpdate) {
    val donor = unsecuredRead(donorUpdate.getDonorId());

    donor.setWithDonor(donorUpdate);
    donorRepository.save(donor);
    infoService.update(donor.getDonorId(), donor.getInfoAsString());
    return OK;
  }

  @Transactional
  public String securedDelete(@NonNull String studyId, @NonNull List<String> ids) {
    ids.forEach(x -> securedDelete(studyId, x));
    return OK;
  }

  @Transactional
  public String securedDelete(@NonNull String studyId, @NonNull String id) {
    checkDonorAndStudyRelated(studyId, id);
    return internalDelete(id);
  }

  private String internalDelete(String id){
    specimenService.deleteByParentId(id);
    donorRepository.deleteById(id);
    infoService.delete(id);
    return OK;
  }

  // TODO: [SONG-254] DeleteByParentId spec missing -- https://github.com/overture-stack/SONG/issues/254
  public String deleteByParentId(@NonNull String studyId) {
    studyService.checkStudyExist(studyId);
    donorRepository.findAllByStudyId(studyId).stream()
        .map(Donor::getDonorId)
        .forEach(this::internalDelete);
    return OK;
  }

  public Optional<String> findByBusinessKey(@NonNull String studyId, @NonNull String donorSubmitterId){
    return donorRepository.findAllByStudyIdAndDonorSubmitterId(studyId, donorSubmitterId)
        .stream()
        .map(Donor::getDonorId)
        .findFirst();
  }

  public String save(@NonNull String studyId, @NonNull Donor donor) {
    donor.setStudyId(studyId);

    val donorIdResult = findByBusinessKey(studyId, donor.getDonorSubmitterId());
    val donorWithSpecimens = new DonorWithSpecimens();
    donorWithSpecimens.setDonor(donor);
    String donorId;
    if (!donorIdResult.isPresent()) {
      donorId = create(donorWithSpecimens);
    } else {
      donorId = donorIdResult.get();
      val updateDonor = donorWithSpecimens.createDonor();
      updateDonor.setDonorId(donorId);
      update(updateDonor);
    }
    return donorId;
  }

}
