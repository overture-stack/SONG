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

package org.icgc.dcc.song.server.service;

import lombok.val;
import org.icgc.dcc.song.server.model.Metadata;
import org.icgc.dcc.song.server.model.Upload;
import org.icgc.dcc.song.server.model.analysis.SequencingReadAnalysis;
import org.icgc.dcc.song.server.model.analysis.VariantCallAnalysis;
import org.icgc.dcc.song.server.model.entity.Donor;
import org.icgc.dcc.song.server.model.entity.Sample;
import org.icgc.dcc.song.server.model.entity.Specimen;
import org.icgc.dcc.song.server.model.entity.Study;
import org.icgc.dcc.song.server.model.entity.composites.CompositeEntity;
import org.icgc.dcc.song.server.model.entity.composites.DonorWithSpecimens;
import org.icgc.dcc.song.server.model.entity.composites.SpecimenWithSamples;
import org.icgc.dcc.song.server.model.entity.composites.StudyWithDonors;
import org.icgc.dcc.song.server.model.entity.FileEntity;
import org.icgc.dcc.song.core.model.enums.FileTypes;
import org.icgc.dcc.song.server.model.enums.UploadStates;
import org.icgc.dcc.song.server.model.experiment.SequencingRead;
import org.icgc.dcc.song.server.model.experiment.VariantCall;
import org.icgc.dcc.song.server.model.legacy.LegacyEntity;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.icgc.dcc.song.server.model.enums.AccessTypes.CONTROLLED;
import static org.icgc.dcc.song.core.model.enums.AnalysisStates.PUBLISHED;
import static org.icgc.dcc.song.core.model.enums.AnalysisStates.SUPPRESSED;
import static org.icgc.dcc.song.core.model.enums.AnalysisStates.UNPUBLISHED;
import static org.icgc.dcc.song.core.model.enums.AnalysisStates.resolveAnalysisState;
import static org.icgc.dcc.song.server.model.enums.Constants.LIBRARY_STRATEGY;
import static org.icgc.dcc.song.server.model.enums.Constants.SAMPLE_TYPE;
import static org.icgc.dcc.song.server.model.enums.Constants.SPECIMEN_CLASS;
import static org.icgc.dcc.song.server.model.enums.Constants.SPECIMEN_TYPE;
import static org.icgc.dcc.song.server.utils.TestFiles.assertInfoKVPair;

public class EntityTest {
  private static final String DEFAULT_STUDY_ID = "ABC123";
  private static final List<String> SPECIMEN_CLASSES = newArrayList(SPECIMEN_CLASS);
  private static final List<String> SPECIMEN_TYPES = newArrayList(SPECIMEN_TYPE);
  private static final List<String> SAMPLE_TYPES = newArrayList(SAMPLE_TYPE);
  private static final List<String> FILE_TYPES = stream(FileTypes.values()).map(FileTypes::toString).collect(toList());
  private static final List<String> LIBRARY_STRATEGIES = newArrayList(LIBRARY_STRATEGY);

  @Test
  public void testCompositeEntity(){
    val donor1 = Donor.builder()
        .donorId("myDonor1")
        .donorSubmitterId("myDonorSubmitter1")
        .studyId(DEFAULT_STUDY_ID)
        .donorGender("male")
        .build();
    val donor2 = Donor.builder()
        .donorId("myDonor2")
        .donorSubmitterId("myDonorSubmitter2")
        .studyId(DEFAULT_STUDY_ID)
        .donorGender("female")
        .build();

    val specimen1 = Specimen.builder()
        .specimenId("mySpecimen1")
        .specimenSubmitterId( "mySpecimenSubmitter1")
        .donorId("myDonor1")
        .specimenClass(SPECIMEN_CLASSES.get(2))
        .specimenType(SPECIMEN_TYPES.get(2))
        .build();

    val specimen2 = Specimen.builder()
        .specimenId("mySpecimen2")
        .specimenSubmitterId("mySpecimenSubmitter2")
        .donorId("myDonor2")
        .specimenClass(SPECIMEN_CLASSES.get(1))
        .specimenType(SPECIMEN_TYPES.get(1))
        .build();

    val sample1 = Sample.builder()
        .sampleId("mySample1")
        .sampleSubmitterId("mySubmitterSample1")
        .specimenId("mySpecimen1")
        .sampleType(SAMPLE_TYPES.get(1))
        .build();

    val sample2 = Sample.builder()
        .sampleId("mySample2")
        .sampleSubmitterId("mySubmitterSample2")
        .specimenId("mySpecimen2")
        .sampleType(SAMPLE_TYPES.get(3))
        .build();

    val compositeEntity1 = CompositeEntity.create(sample1);
    compositeEntity1.setDonor(donor1);
    compositeEntity1.setSpecimen(specimen1);

    val compositeEntity1_same = new CompositeEntity();
    compositeEntity1_same.setSampleType(SAMPLE_TYPES.get(1));
    compositeEntity1_same.setSampleSubmitterId("mySubmitterSample1");
    compositeEntity1_same.setSampleId("mySample1");
    compositeEntity1_same.setSpecimenId("mySpecimen1");
    compositeEntity1_same.setSpecimen(specimen1);
    compositeEntity1_same.setDonor(donor1);

    assertEntitiesEqual(compositeEntity1, compositeEntity1_same, true);

    val compositeEntity2 = CompositeEntity.create(sample1);
    compositeEntity2.setDonor(donor1);
    compositeEntity2.setSpecimen(specimen2);
    assertEntitiesNotEqual(compositeEntity1, compositeEntity2);

    compositeEntity2.setDonor(donor2);
    compositeEntity2.setSpecimen(specimen1);
    assertEntitiesNotEqual(compositeEntity1, compositeEntity2);

    compositeEntity2.setDonor(donor2);
    compositeEntity2.setSpecimen(specimen2);
    assertEntitiesNotEqual(compositeEntity1, compositeEntity2);

    val compositeEntity3 = CompositeEntity.create(sample2);
    compositeEntity3.setDonor(donor1);
    compositeEntity3.setSpecimen(specimen2);
    assertEntitiesNotEqual(compositeEntity1, compositeEntity3);

    compositeEntity3.setDonor(donor2);
    compositeEntity3.setSpecimen(specimen1);
    assertEntitiesNotEqual(compositeEntity1, compositeEntity3);

    compositeEntity3.setDonor(donor2);
    compositeEntity3.setSpecimen(specimen2);
    assertEntitiesNotEqual(compositeEntity1, compositeEntity3);

    compositeEntity3.setDonor(donor1);
    compositeEntity3.setSpecimen(specimen1);
    assertEntitiesNotEqual(compositeEntity1, compositeEntity3);

    compositeEntity1.setInfo("key1", "f5c9381090a53c54358feb2ba5b7a3d7");
    compositeEntity1_same.setInfo("key2", "6329334b-dcd5-53c8-98fd-9812ac386d30");
    assertEntitiesNotEqual(compositeEntity1, compositeEntity1_same);

    // Test getters

    assertThat(compositeEntity1.getSampleId()).isEqualTo("mySample1");
    assertThat(compositeEntity1.getSampleType()).isEqualTo(SAMPLE_TYPES.get(1));
    assertThat(compositeEntity1.getSampleSubmitterId()).isEqualTo("mySubmitterSample1");
    assertThat(compositeEntity1.getSpecimenId()).isEqualTo("mySpecimen1");
    assertThat(compositeEntity1.getSpecimen()).isEqualTo(specimen1);
    assertThat(compositeEntity1.getDonor()).isEqualTo(donor1);
    assertInfoKVPair(compositeEntity1, "key1", "f5c9381090a53c54358feb2ba5b7a3d7");
  }

  @Test
  public void testDonorWithSpecimens(){
    val specimen1 = Specimen.builder()
        .specimenId("mySpecimen1")
        .specimenSubmitterId("mySpecimenSubmitter1")
        .donorId("myDonor1")
        .specimenClass(SPECIMEN_CLASSES.get(2))
        .specimenType(SPECIMEN_TYPES.get(2))
        .build();

    val specimen2 = Specimen.builder()
        .specimenId("mySpecimen2")
        .specimenSubmitterId("mySpecimenSubmitter2")
        .donorId("myDonor2")
        .specimenClass(SPECIMEN_CLASSES.get(1))
        .specimenType(SPECIMEN_TYPES.get(1))
        .build();

    val sample11 = Sample.builder()
        .sampleId("mySample11")
        .sampleSubmitterId("mySubmitterSample11")
        .specimenId("mySpecimen11")
        .sampleType(SAMPLE_TYPES.get(2))
        .build();

    val sample12 = Sample.builder()
        .sampleId("mySample12")
        .sampleSubmitterId("mySubmitterSample12")
        .specimenId("mySpecimen12")
        .sampleType(SAMPLE_TYPES.get(2))
        .build();

    val sampleGroup1 = newArrayList(sample11, sample12);

    val sample21 = Sample.builder()
        .sampleId("mySample21")
        .sampleSubmitterId("mySubmitterSample21")
        .specimenId("mySpecimen21")
        .sampleType(SAMPLE_TYPES.get(3))
        .build();

    val sample22 = Sample.builder()
        .sampleId("mySample22")
        .sampleSubmitterId("mySubmitterSample22")
        .specimenId("mySpecimen22")
        .sampleType(SAMPLE_TYPES.get(3))
        .build();

    val sampleGroup2 = newArrayList(sample21, sample22);

    val specimenWithSample1 = new SpecimenWithSamples();
    specimenWithSample1.setSpecimen(specimen1);
    specimenWithSample1.setSamples(sampleGroup1);

    val specimenWithSample2 = new SpecimenWithSamples();
    specimenWithSample2.setSpecimen(specimen2);
    specimenWithSample2.setSamples(sampleGroup2);

    val specimenWSampleGroup1 = newArrayList(specimenWithSample1, specimenWithSample2);
    val specimenWSampleGroup2 = newArrayList(specimenWithSample1);


    val donor1 = Donor.builder()
        .donorId("myDonor1")
        .donorSubmitterId("myDonorSubmitter1")
        .studyId(DEFAULT_STUDY_ID)
        .donorGender("male")
        .build();
    val donor2 = Donor.builder()
        .donorId("myDonor2")
        .donorSubmitterId("myDonorSubmitter2")
        .studyId(DEFAULT_STUDY_ID)
        .donorGender("female")
        .build();

    val d1 = new DonorWithSpecimens();
    d1.setDonorGender(donor1.getDonorGender());
    d1.setDonorId(donor1.getDonorId());
    d1.setDonorSubmitterId(donor1.getDonorSubmitterId());
    d1.setStudyId(donor1.getStudyId());

    val d2 =  new DonorWithSpecimens();
    d2.setDonor(donor1);

    assertEntitiesEqual(d1, d2, true);

    //00 - matchingDonors=0   matchingSpecimenGroups=0
    d1.setDonor(donor1);
    d1.setSpecimens(specimenWSampleGroup1);
    d2.setDonor(donor2);
    d2.setSpecimens(specimenWSampleGroup2);
    assertEntitiesNotEqual(d1, d2);

    //01 - matchingDonors=0   matchingSpecimenGroups=1
    d1.setDonor(donor1);
    d1.setSpecimens(specimenWSampleGroup1);
    d2.setDonor(donor2);
    d2.setSpecimens(specimenWSampleGroup1);
    assertEntitiesNotEqual(d1, d2);

    //10 - matchingDonors=1   matchingSpecimenGroups=0
    d1.setDonor(donor1);
    d1.setSpecimens(specimenWSampleGroup1);
    d2.setDonor(donor1);
    d2.setSpecimens(specimenWSampleGroup2);
    assertEntitiesNotEqual(d1, d2);

    //11 - matchingDonors=1   matchingSpecimenGroups=1
    d1.setDonor(donor1);
    d1.setSpecimens(specimenWSampleGroup1);
    d2.setDonor(donor1);
    d2.setSpecimens(specimenWSampleGroup1);
    assertEntitiesEqual(d1, d2, true);

    d1.setInfo("key1", "f5c9381090a53c54358feb2ba5b7a3d7");
    d2.setInfo("key2", "6329334b-dcd5-53c8-98fd-9812ac386d30");
    assertEntitiesNotEqual(d1, d2);

    //Test getters
    assertThat(d1.getDonorGender()).isEqualTo(donor1.getDonorGender());
    assertThat(d1.getDonorSubmitterId()).isEqualTo(donor1.getDonorSubmitterId());
    assertThat(d1.getDonorId()).isEqualTo(donor1.getDonorId());
    assertThat(d1.getStudyId()).isEqualTo(donor1.getStudyId());
    assertThat(d1.getSpecimens()).containsExactlyInAnyOrder(specimenWithSample1, specimenWithSample2);
    assertThat(d1.createDonor()).isNotEqualTo(donor1);
    assertInfoKVPair(d1, "key1", "f5c9381090a53c54358feb2ba5b7a3d7");
  }

  @Test
  public void testSpecimenWithSamples(){
    val specimen1 = Specimen.builder()
        .specimenId("mySpecimen1")
        .specimenSubmitterId("mySpecimenSubmitter1")
        .donorId("myDonor1")
        .specimenClass(SPECIMEN_CLASSES.get(2))
        .specimenType(SPECIMEN_TYPES.get(2))
        .build();

    val specimen2 = Specimen.builder()
        .specimenId("mySpecimen2")
        .specimenSubmitterId("mySpecimenSubmitter2")
        .donorId("myDonor2")
        .specimenClass(SPECIMEN_CLASSES.get(1))
        .specimenType(SPECIMEN_TYPES.get(1))
        .build();

    val sample11 = Sample.builder()
        .sampleId("mySample11")
        .sampleSubmitterId("mySubmitterSample11")
        .specimenId("mySpecimen11")
        .sampleType(SAMPLE_TYPES.get(2))
        .build();

    val sample12 = Sample.builder()
        .sampleId("mySample12")
        .sampleSubmitterId("mySubmitterSample12")
        .specimenId("mySpecimen12")
        .sampleType(SAMPLE_TYPES.get(2))
        .build();

    val sampleGroup1 = newArrayList(sample11, sample12);

    val sample21 = Sample.builder()
        .sampleId("mySample21")
        .sampleSubmitterId("mySubmitterSample21")
        .specimenId("mySpecimen21")
        .sampleType(SAMPLE_TYPES.get(3))
        .build();

    val sample22 = Sample.builder()
        .sampleId("mySample22")
        .sampleSubmitterId("mySubmitterSample22")
        .specimenId("mySpecimen22")
        .sampleType(SAMPLE_TYPES.get(3))
        .build();

    val sampleGroup2 = newArrayList(sample21, sample22);

    val s1 = new SpecimenWithSamples();
    s1.setDonorId(specimen1.getDonorId());
    s1.setSpecimenClass(specimen1.getSpecimenClass());
    s1.setSpecimenSubmitterId(specimen1.getSpecimenSubmitterId());
    s1.setSpecimenId(specimen1.getSpecimenId());
    s1.setSpecimenType(specimen1.getSpecimenType());

    val s2 = new SpecimenWithSamples();
    s2.setSpecimen(specimen1);

    assertEntitiesEqual(s1, s2, true);

    // 00 - matchingSampleGroup=0   matchingSpecimen=0
    s1.setSpecimen(specimen1);
    s1.setSamples(sampleGroup1);
    s2.setSpecimen(specimen2);
    s2.setSamples(sampleGroup2);
    assertEntitiesNotEqual(s1, s2);

    // 01 - matchingSampleGroup=0   matchingSpecimen=1
    s1.setSpecimen(specimen1);
    s1.setSamples(sampleGroup1);
    s2.setSpecimen(specimen1);
    s2.setSamples(sampleGroup2);
    assertEntitiesNotEqual(s1, s2);

    // 10 - matchingSampleGroup=1   matchingSpecimen=0
    s1.setSpecimen(specimen1);
    s1.setSamples(sampleGroup1);
    s2.setSpecimen(specimen2);
    s2.setSamples(sampleGroup1);
    assertEntitiesNotEqual(s1, s2);

    // 11 - matchingSampleGroup=1   matchingSpecimen=1
    s1.setSpecimen(specimen1);
    s1.setSamples(sampleGroup1);
    s2.setSpecimen(specimen1);
    s2.setSamples(sampleGroup1);
    assertEntitiesEqual(s1, s2, true);

    s1.setInfo("key1", "f5c9381090a53c54358feb2ba5b7a3d7");
    s2.setInfo("key2", "6329334b-dcd5-53c8-98fd-9812ac386d30");
    assertEntitiesNotEqual(s1,s2);

    //Test getters
    assertThat(s1.getDonorId()).isEqualTo(specimen1.getDonorId());
    assertThat(s1.getSpecimenClass()).isEqualTo(specimen1.getSpecimenClass());
    assertThat(s1.getSpecimenSubmitterId()).isEqualTo(specimen1.getSpecimenSubmitterId());
    assertThat(s1.getSpecimenType()).isEqualTo(specimen1.getSpecimenType());
    assertThat(s1.getSpecimenId()).isEqualTo(specimen1.getSpecimenId());
    assertThat(s1.getSamples()).containsExactlyInAnyOrder(sample11, sample12);
    assertInfoKVPair(s1, "key1", "f5c9381090a53c54358feb2ba5b7a3d7");

    // Test addSample
    val sLeft = new SpecimenWithSamples();
    sLeft.setDonorId(specimen1.getDonorId());
    sLeft.setSpecimenClass(specimen1.getSpecimenClass());
    sLeft.setSpecimenSubmitterId(specimen1.getSpecimenSubmitterId());
    sLeft.setSpecimenId(specimen1.getSpecimenId());
    sLeft.setSpecimenType(specimen1.getSpecimenType());
    sLeft.setSamples(sampleGroup2);

    val sRight = new SpecimenWithSamples();
    sRight.setDonorId(specimen1.getDonorId());
    sRight.setSpecimenClass(specimen1.getSpecimenClass());
    sRight.setSpecimenSubmitterId(specimen1.getSpecimenSubmitterId());
    sRight.setSpecimenId(specimen1.getSpecimenId());
    sRight.setSpecimenType(specimen1.getSpecimenType());
    sampleGroup2.forEach(sRight::addSample);

    assertThat(sLeft).isEqualTo(sRight);
  }

  @Test
  public void testStudyWithDonors(){
    val study1 = Study.builder()
        .studyId("d1")
        .name("b1")
        .organization("c1")
        .description("a1")
        .build();

    val study2 = Study.builder()
        .studyId("d2")
        .name("b2")
        .organization("c2")
        .description("a2")
        .build();

    val s1 = new StudyWithDonors();
    s1.setStudy(study1);

    val s1_same = new StudyWithDonors();
    s1_same.setName(study1.getName());
    s1_same.setOrganization(study1.getOrganization());
    s1_same.setStudyId(study1.getStudyId());
    s1_same.setDescription(study1.getDescription());
    assertEntitiesEqual(s1, s1_same, true);

    val s2 = new StudyWithDonors();
    s2.setStudy(study2);
    assertEntitiesNotEqual(s1,s2);

    //---------------
    val specimen1 = Specimen.builder()
        .specimenId("mySpecimen1")
        .specimenSubmitterId("mySpecimenSubmitter1")
        .donorId("myDonor1")
        .specimenClass(SPECIMEN_CLASSES.get(2))
        .specimenType(SPECIMEN_TYPES.get(2))
        .build();

    val specimen2 = Specimen.builder()
        .specimenId("mySpecimen2")
        .specimenSubmitterId("mySpecimenSubmitter2")
        .donorId("myDonor2")
        .specimenClass(SPECIMEN_CLASSES.get(1))
        .specimenType(SPECIMEN_TYPES.get(1))
        .build();

    val sample11 = Sample.builder()
        .sampleId("mySample11")
        .sampleSubmitterId("mySubmitterSample11")
        .specimenId("mySpecimen11")
        .sampleType(SAMPLE_TYPES.get(2))
        .build();

    val sample12 = Sample.builder()
        .sampleId("mySample12")
        .sampleSubmitterId("mySubmitterSample12")
        .specimenId("mySpecimen12")
        .sampleType(SAMPLE_TYPES.get(2))
        .build();

    val sampleGroup1 = newArrayList(sample11, sample12);

    val sample21 = Sample.builder()
        .sampleId("mySample21")
        .sampleSubmitterId("mySubmitterSample21")
        .specimenId("mySpecimen21")
        .sampleType(SAMPLE_TYPES.get(3))
        .build();

    val sample22 = Sample.builder()
        .sampleId("mySample22")
        .sampleSubmitterId("mySubmitterSample22")
        .specimenId("mySpecimen22")
        .sampleType(SAMPLE_TYPES.get(3))
        .build();

    val sampleGroup2 = newArrayList(sample21, sample22);


    val specimenWithSample1 = new SpecimenWithSamples();
    specimenWithSample1.setSpecimen(specimen1);
    specimenWithSample1.setSamples(sampleGroup1);

    val specimenWithSample2 = new SpecimenWithSamples();
    specimenWithSample2.setSpecimen(specimen2);
    specimenWithSample2.setSamples(sampleGroup2);

    val specimenWSampleGroup1 = newArrayList(specimenWithSample1, specimenWithSample2);
    val specimenWSampleGroup2 = newArrayList(specimenWithSample1);


    val donor1 = Donor.builder()
        .donorId("myDonor1")
        .donorSubmitterId("myDonorSubmitter1")
        .studyId(DEFAULT_STUDY_ID)
        .donorGender("male")
        .build();
    val donor2 = Donor.builder()
        .donorId("myDonor2")
        .donorSubmitterId("myDonorSubmitter2")
        .studyId(DEFAULT_STUDY_ID)
        .donorGender("female")
        .build();

    val d1 = new DonorWithSpecimens();
    d1.setDonor(donor1);
    d1.setSpecimens(specimenWSampleGroup1);

    val d2 =  new DonorWithSpecimens();
    d2.setDonor(donor2);
    d2.setSpecimens(specimenWSampleGroup2);


    // 00 -- matchingDonorGroup=0    matchingStudy=0
    s1.setStudy(study1);
    s1.setDonors(newArrayList(d1));
    s2.setStudy(study2);
    s2.setDonors(newArrayList(d2));
    assertEntitiesNotEqual(s1,s2);

    // 01 -- matchingDonorGroup=0    matchingStudy=1
    s1.setStudy(study1);
    s1.setDonors(newArrayList(d1));
    s2.setStudy(study1);
    s2.setDonors(newArrayList(d2));
    assertEntitiesNotEqual(s1,s2);

    // 10 -- matchingDonorGroup=1    matchingStudy=0
    s1.setStudy(study1);
    s1.setDonors(newArrayList(d1));
    s2.setStudy(study2);
    s2.setDonors(newArrayList(d1));
    assertEntitiesNotEqual(s1,s2);

    // 11 -- matchingDonorGroup=1    matchingStudy=1
    s1.setStudy(study1);
    s1.setDonors(newArrayList(d1));
    s2.setStudy(study1);
    s2.setDonors(newArrayList(d1));
    assertEntitiesEqual(s1, s2, true);

    s1.setInfo("key1", "f5c9381090a53c54358feb2ba5b7a3d7");
    s2.setInfo("key2", "6329334b-dcd5-53c8-98fd-9812ac386d30");
    assertInfoKVPair(s1, "key1", "f5c9381090a53c54358feb2ba5b7a3d7");
    assertEntitiesNotEqual(s1,s2);

    s1.setInfo("key1", "f5c9381090a53c54358feb2ba5b7a3d7");
    s2.setInfo("key1", "6329334b-dcd5-53c8-98fd-9812ac386d30");
    assertEntitiesNotEqual(s1,s2);


    //Test getters
    assertThat(s1.getDescription()).isEqualTo(study1.getDescription());
    assertThat(s1.getName()).isEqualTo(study1.getName());
    assertThat(s1.getOrganization()).isEqualTo(study1.getOrganization());
    assertThat(s1.getStudyId()).isEqualTo(study1.getStudyId());
    assertThat(s1.getStudy()).isNotSameAs(study1);
    assertThat(s1.getDonors()).containsExactlyInAnyOrder(d1);
    assertInfoKVPair(s1, "key1", "f5c9381090a53c54358feb2ba5b7a3d7");

    // Assert addDonor
    val sLeft = new StudyWithDonors();
    sLeft.setStudy(study1);
    sLeft.setDonors(newArrayList(d1, d2));

    val sRight = new StudyWithDonors();
    sRight.setStudy(study1);
    sRight.addDonor(d1);
    sRight.addDonor(d2);
    assertThat(sLeft).isEqualTo(sRight);

  }


  @Test
  public void testFile(){
    val file1 = new FileEntity();
    file1.setAnalysisId("a1");
    file1.setFileAccess(CONTROLLED);
    file1.setFileMd5sum("b1");
    file1.setFileName("c1");
    file1.setFileSize(13L);
    file1.setFileType(FILE_TYPES.get(0));
    file1.setObjectId("d1");
    file1.setStudyId("e1");

    val file1_same = FileEntity.builder()
        .objectId("d1")
        .analysisId("a1")
        .fileName("c1")
        .studyId("e1")
        .fileSize(13L)
        .fileType(FILE_TYPES.get(0))
        .fileMd5sum("b1")
        .fileAccess(CONTROLLED.toString())
        .build();
    assertEntitiesEqual(file1, file1_same, true);

    val file2 = FileEntity.builder()
        .objectId("d2")
        .analysisId("a2")
        .fileName("c2")
        .studyId("e2")
        .fileSize(14L)
        .fileType(FILE_TYPES.get(1))
        .fileMd5sum("b2")
        .fileAccess(CONTROLLED.toString())
        .build();

    assertEntitiesNotEqual(file1, file2);

    file1.setInfo("key1", "f5c9381090a53c54358feb2ba5b7a3d7");
    file1_same.setInfo("key2", "6329334b-dcd5-53c8-98fd-9812ac386d30");
    assertEntitiesNotEqual(file1, file1_same);

    // Test getters
    assertThat(file1.getAnalysisId()).isEqualTo("a1");
    assertThat(file1.getFileAccess()).isEqualTo(CONTROLLED.toString());
    assertThat(file1.getFileMd5sum()).isEqualTo("b1");
    assertThat(file1.getFileName()).isEqualTo("c1");
    assertThat(file1.getFileType()).isEqualTo(FILE_TYPES.get(0));
    assertThat(file1.getObjectId()).isEqualTo("d1");
    assertThat(file1.getStudyId()).isEqualTo("e1");
    assertInfoKVPair(file1, "key1", "f5c9381090a53c54358feb2ba5b7a3d7");

  }

  @Test
  public void testSample(){
    val sample1 = new Sample();
    sample1.setSampleId("a1");
    sample1.setSampleSubmitterId("b1");
    sample1.setSampleType(SAMPLE_TYPES.get(0));
    sample1.setSpecimenId("c1");

    val sample1_same = Sample.builder()
        .sampleId("a1")
        .sampleSubmitterId("b1")
        .sampleType(SAMPLE_TYPES.get(0))
        .specimenId("c1")
        .build();
    assertEntitiesEqual(sample1, sample1_same, true);

    val sample2 = Sample.builder()
        .sampleId("a2")
        .sampleSubmitterId("b2")
        .sampleType(SAMPLE_TYPES.get(1))
        .specimenId("c2")
        .build();
    assertEntitiesNotEqual(sample1, sample2);

    sample1.setInfo("key1", "f5c9381090a53c54358feb2ba5b7a3d7");
    sample1_same.setInfo("key2", "6329334b-dcd5-53c8-98fd-9812ac386d30");
    assertEntitiesNotEqual(sample1, sample1_same);

    // Test getters
    assertThat(sample1.getSampleId()).isEqualTo("a1");
    assertThat(sample1.getSampleSubmitterId()).isEqualTo("b1");
    assertThat(sample1.getSampleType()).isEqualTo(SAMPLE_TYPES.get(0));
    assertThat(sample1.getSpecimenId()).isEqualTo("c1");
    assertInfoKVPair(sample1, "key1", "f5c9381090a53c54358feb2ba5b7a3d7");
  }

  @Test
  public void testMetadata(){
    val metadata1 = new Metadata();
    metadata1.setInfo("key1", "f5c9381090a53c54358feb2ba5b7a3d7");

    val metadata1_same = new Metadata();
    metadata1_same.setInfo("key1", "f5c9381090a53c54358feb2ba5b7a3d7");

    assertEntitiesEqual(metadata1, metadata1_same, true);

    val metadata2 = new Metadata();
    metadata2.setInfo("key2", "6329334b-dcd5-53c8-98fd-9812ac386d30");

    assertEntitiesNotEqual(metadata1, metadata2);

    metadata2.addInfo(metadata1.getInfoAsString());
    assertThat(metadata2.getInfo().has("key1")).isTrue();
    assertThat(metadata2.getInfo().path("key1").textValue())
        .isEqualTo("f5c9381090a53c54358feb2ba5b7a3d7");

    metadata2.setInfo("something that is not json");
    assertThat(metadata2.getInfo().has("info")).isTrue();
    assertThat(metadata2.getInfo().path("info").textValue()).isEqualTo("something that is not json");
  }

  @Test
  public void testUpload(){
    val u1 = new Upload();
    u1.setAnalysisId("an1");
    u1.setCreatedAt(LocalDateTime.MAX);
    u1.setErrors("error1");
    u1.setPayload("payload1");
    u1.setState(UploadStates.CREATED);
    u1.setState(UploadStates.CREATED.getText());
    u1.setStudyId(DEFAULT_STUDY_ID);
    u1.setUpdatedAt(LocalDateTime.MIN);
    u1.setUploadId("uploadId1");

    val u1_same = Upload.builder()
        .uploadId("uploadId1")
        .studyId(DEFAULT_STUDY_ID)
        .analysisId("an1")
        .state(UploadStates.CREATED.toString())
        .errors("error1")
        .payload("payload1")
        .updatedAt(LocalDateTime.MIN)
        .createdAt(LocalDateTime.MAX)
        .build();
    assertEntitiesEqual(u1, u1_same, true);

    val u2 = Upload.builder()
        .uploadId("uploadId2")
        .studyId("study333")
        .analysisId("an2")
        .state(UploadStates.VALIDATION_ERROR.toString())
        .errors("error2")
        .payload("payload2")
        .updatedAt(LocalDateTime.MIN)
        .createdAt(LocalDateTime.MAX)
        .build();
    assertEntitiesNotEqual(u1,u2);

    //Test getters
    assertThat(u1.getAnalysisId()).isEqualTo("an1");
    assertThat(u1.getCreatedAt()).isEqualTo(LocalDateTime.MAX);
    assertThat(u1.getErrors()).isEqualTo(newArrayList("error1"));
    assertThat(u1.getPayload()).isEqualTo("payload1");
    assertThat(u1.getState()).isEqualTo(UploadStates.CREATED.getText());
    assertThat(u1.getStudyId()).isEqualTo(DEFAULT_STUDY_ID);
    assertThat(u1.getUpdatedAt()).isEqualTo(LocalDateTime.MIN);
    assertThat(u1.getUploadId()).isEqualTo("uploadId1");

    u1.setErrors("error1|error2|error3");
    assertThat(u1.getErrors()).containsExactlyInAnyOrder("error1", "error2", "error3");
    assertThat(u1.getErrors()).hasSize(3);

    u1.addErrors(newArrayList("error4", "error5"));
    assertThat(u1.getErrors()).containsExactlyInAnyOrder("error1", "error2", "error3", "error4", "error5");
    assertThat(u1.getErrors()).hasSize(5);
  }

  @Test
  public void testLegacyEntity(){
    val e1 = LegacyEntity.builder()
        .access("open")
        .fileName("f1")
        .gnosId("g1")
        .id("i1")
        .projectCode("p1")
        .build();

    val e1_same = LegacyEntity.builder()
        .access("open")
        .fileName("f1")
        .gnosId("g1")
        .id("i1")
        .projectCode("p1")
        .build();

    assertEntitiesEqual(e1, e1_same, true);

    val e2 = LegacyEntity.builder()
        .access("open")
        .fileName("f2")
        .gnosId("g2")
        .id("i2")
        .projectCode("p2")
        .build();
    assertEntitiesNotEqual(e1, e2);

    // Test getters
    assertThat(e1.getAccess()).isEqualTo("open");
    assertThat(e1.getFileName()).isEqualTo("f1");
    assertThat(e1.getGnosId()).isEqualTo("g1");
    assertThat(e1.getId()).isEqualTo("i1");
    assertThat(e1.getProjectCode()).isEqualTo("p1");
  }

  @Test
  public void testVariantCallAnalysis(){
    val v1 = VariantCall.builder()
        .analysisId("a1")
        .variantCallingTool("c1")
        .matchedNormalSampleSubmitterId("b1")
        .build();

    val v2 = VariantCall.builder()
        .analysisId("a2")
        .variantCallingTool("c2")
        .matchedNormalSampleSubmitterId("b2")
        .build();

    val donor1 = Donor.builder()
        .donorId("myDonor1")
        .donorSubmitterId("myDonorSubmitter1")
        .studyId(DEFAULT_STUDY_ID)
        .donorGender("male")
        .build();
    val donor2 = Donor.builder()
        .donorId("myDonor2")
        .donorSubmitterId("myDonorSubmitter2")
        .studyId(DEFAULT_STUDY_ID)
        .donorGender("female")
        .build();

    val specimen1 = Specimen.builder()
        .specimenId("mySpecimen1")
        .specimenSubmitterId("mySpecimenSubmitter1")
        .donorId("myDonor1")
        .specimenClass(SPECIMEN_CLASSES.get(2))
        .specimenType(SPECIMEN_TYPES.get(2))
        .build();

    val specimen2 = Specimen.builder()
        .specimenId("mySpecimen2")
        .specimenSubmitterId("mySpecimenSubmitter2")
        .donorId("myDonor2")
        .specimenClass(SPECIMEN_CLASSES.get(1))
        .specimenType(SPECIMEN_TYPES.get(1))
        .build();

    val sample1 = Sample.builder()
        .sampleId("mySample1")
        .sampleSubmitterId("mySubmitterSample1")
        .specimenId("mySpecimen1")
        .sampleType(SAMPLE_TYPES.get(2))
        .build();

    val sample2 = Sample.builder()
        .sampleId("mySample2")
        .sampleSubmitterId("mySubmitterSample2")
        .specimenId("mySpecimen2")
        .sampleType(SAMPLE_TYPES.get(3))
        .build();

    val compositeEntity11 = CompositeEntity.create(sample1);
    compositeEntity11.setDonor(donor1);
    compositeEntity11.setSpecimen(specimen2);

    val compositeEntity12 = CompositeEntity.create(sample1);
    compositeEntity12.setDonor(donor2);
    compositeEntity12.setSpecimen(specimen1);

    val compositeGroup1 = newArrayList(compositeEntity11, compositeEntity12);

    val compositeEntity21 = CompositeEntity.create(sample2);
    compositeEntity21.setDonor(donor1);
    compositeEntity21.setSpecimen(specimen2);

    val compositeEntity22 = CompositeEntity.create(sample2);
    compositeEntity22.setDonor(donor2);
    compositeEntity22.setSpecimen(specimen1);

    val compositeGroup2 = newArrayList(compositeEntity21, compositeEntity22);

    val file11 = FileEntity.builder()
        .objectId("d11")
        .analysisId("a11")
        .fileName("c11")
        .studyId("e11")
        .fileSize(113L)
        .fileType(FILE_TYPES.get(0))
        .fileMd5sum("b11")
        .fileAccess(CONTROLLED.toString())
        .build();

    val file12 = FileEntity.builder()
        .objectId("d12")
        .analysisId("a12")
        .fileName("c12")
        .studyId("e12")
        .fileSize(114L)
        .fileType(FILE_TYPES.get(0))
        .fileMd5sum("b12")
        .fileAccess(CONTROLLED.toString())
        .build();

    val fileGroup1 = newArrayList(file11, file12);

    val file21 = FileEntity.builder()
        .objectId("d21")
        .analysisId("a21")
        .fileName("c21")
        .studyId("e21")
        .fileSize(213L)
        .fileType(FILE_TYPES.get(1))
        .fileMd5sum("b21")
        .fileAccess(CONTROLLED.toString())
        .build();

    val file22 = FileEntity.builder()
        .objectId("d22")
        .analysisId("a22")
        .fileName("c22")
        .studyId("e22")
        .fileSize(214L)
        .fileType(FILE_TYPES.get(1))
        .fileMd5sum("b22")
        .fileAccess(CONTROLLED.toString())
        .build();

    val fileGroup2 = newArrayList(file21, file22);

    val a1 = new VariantCallAnalysis();
    a1.setAnalysisId("a1");
    a1.setAnalysisState(PUBLISHED.toString());
    a1.setStudy("b1");

    val a1_same = new VariantCallAnalysis();
    a1_same.setAnalysisId("a1");
    a1_same.setAnalysisState(PUBLISHED.toString());
    a1_same.setStudy("b1");

    assertEntitiesEqual(a1, a1_same, true);

    val a2 = new VariantCallAnalysis();
    a2.setAnalysisId("a2");
    a2.setAnalysisState(UNPUBLISHED.toString());
    a2.setStudy("b2");

    val a2_same = new VariantCallAnalysis();
    a2_same.setAnalysisId("a2");
    a2_same.setAnalysisState(UNPUBLISHED.toString());
    a2_same.setStudy("b2");

    assertEntitiesEqual(a2, a2_same, true);

    // 0000 - matchingSamples=0, matchingFile=0, matchingExperiment=0,  matchingSelf=0
    a1.setExperiment(v1);
    a1.setFile(fileGroup1);
    a1.setSample(compositeGroup1);

    a2.setExperiment(v2);
    a2.setFile(fileGroup2);
    a2.setSample(compositeGroup2);
    assertEntitiesNotEqual(a1, a2);

    // 0001 - matchingSamples=0, matchingFile=0, matchingExperiment=0,  matchingSelf=1
    a1.setExperiment(v1);
    a1.setFile(fileGroup1);
    a1.setSample(compositeGroup1);

    a1_same.setExperiment(v2);
    a1_same.setFile(fileGroup2);
    a1_same.setSample(compositeGroup2);
    assertEntitiesNotEqual(a1, a2);

    // 0010 - matchingSamples=0, matchingFile=0, matchingExperiment=1,  matchingSelf=0
    a1.setExperiment(v1);
    a1.setFile(fileGroup1);
    a1.setSample(compositeGroup1);

    a2.setExperiment(v1);
    a2.setFile(fileGroup2);
    a2.setSample(compositeGroup2);
    assertEntitiesNotEqual(a1, a2);

    // 0011 - matchingSamples=0, matchingFile=0, matchingExperiment=1,  matchingSelf=1
    a1.setExperiment(v1);
    a1.setFile(fileGroup1);
    a1.setSample(compositeGroup1);

    a1_same.setExperiment(v1);
    a1_same.setFile(fileGroup2);
    a1_same.setSample(compositeGroup2);
    assertEntitiesNotEqual(a1, a2);

    // 0100 - matchingSamples=0, matchingFile=1, matchingExperiment=0,  matchingSelf=0
    a1.setExperiment(v1);
    a1.setFile(fileGroup1);
    a1.setSample(compositeGroup1);

    a2.setExperiment(v2);
    a2.setFile(fileGroup1);
    a2.setSample(compositeGroup2);
    assertEntitiesNotEqual(a1, a2);

    // 0101 - matchingSamples=0, matchingFile=1, matchingExperiment=0,  matchingSelf=1
    a1.setExperiment(v1);
    a1.setFile(fileGroup1);
    a1.setSample(compositeGroup1);

    a1_same.setExperiment(v2);
    a1_same.setFile(fileGroup1);
    a1_same.setSample(compositeGroup2);
    assertEntitiesNotEqual(a1, a2);

    // 0110 - matchingSamples=0, matchingFile=1, matchingExperiment=1,  matchingSelf=0
    a1.setExperiment(v1);
    a1.setFile(fileGroup1);
    a1.setSample(compositeGroup1);

    a2.setExperiment(v1);
    a2.setFile(fileGroup1);
    a2.setSample(compositeGroup2);
    assertEntitiesNotEqual(a1, a2);

    // 0111 - matchingSamples=0, matchingFile=1, matchingExperiment=1,  matchingSelf=1
    a1.setExperiment(v1);
    a1.setFile(fileGroup1);
    a1.setSample(compositeGroup1);

    a1_same.setExperiment(v1);
    a1_same.setFile(fileGroup1);
    a1_same.setSample(compositeGroup2);
    assertEntitiesNotEqual(a1, a2);

    // 1000 - matchingSamples=1, matchingFile=0, matchingExperiment=0,  matchingSelf=0
    a1.setExperiment(v1);
    a1.setFile(fileGroup1);
    a1.setSample(compositeGroup1);

    a2.setExperiment(v2);
    a2.setFile(fileGroup2);
    a2.setSample(compositeGroup1);
    assertEntitiesNotEqual(a1, a2);

    // 1001 - matchingSamples=1, matchingFile=0, matchingExperiment=0,  matchingSelf=1
    a1.setExperiment(v1);
    a1.setFile(fileGroup1);
    a1.setSample(compositeGroup1);

    a1_same.setExperiment(v2);
    a1_same.setFile(fileGroup2);
    a1_same.setSample(compositeGroup1);
    assertEntitiesNotEqual(a1, a2);

    // 1010 - matchingSamples=1, matchingFile=0, matchingExperiment=1,  matchingSelf=0
    a1.setExperiment(v1);
    a1.setFile(fileGroup1);
    a1.setSample(compositeGroup1);

    a2.setExperiment(v1);
    a2.setFile(fileGroup2);
    a2.setSample(compositeGroup1);
    assertEntitiesNotEqual(a1, a2);

    // 1011 - matchingSamples=1, matchingFile=0, matchingExperiment=1,  matchingSelf=1
    a1.setExperiment(v1);
    a1.setFile(fileGroup1);
    a1.setSample(compositeGroup1);

    a1_same.setExperiment(v1);
    a1_same.setFile(fileGroup2);
    a1_same.setSample(compositeGroup1);
    assertEntitiesNotEqual(a1, a2);

    // 1100 - matchingSamples=1, matchingFile=1, matchingExperiment=0,  matchingSelf=0
    a1.setExperiment(v1);
    a1.setFile(fileGroup1);
    a1.setSample(compositeGroup1);

    a2.setExperiment(v2);
    a2.setFile(fileGroup1);
    a2.setSample(compositeGroup1);
    assertEntitiesNotEqual(a1, a2);

    // 1101 - matchingSamples=1, matchingFile=1, matchingExperiment=0,  matchingSelf=1
    a1.setExperiment(v1);
    a1.setFile(fileGroup1);
    a1.setSample(compositeGroup1);

    a1_same.setExperiment(v2);
    a1_same.setFile(fileGroup1);
    a1_same.setSample(compositeGroup1);
    assertEntitiesNotEqual(a1, a2);

    // 1110 - matchingSamples=1, matchingFile=1, matchingExperiment=1,  matchingSelf=0
    a1.setExperiment(v1);
    a1.setFile(fileGroup1);
    a1.setSample(compositeGroup1);

    a2.setExperiment(v1);
    a2.setFile(fileGroup1);
    a2.setSample(compositeGroup1);
    assertEntitiesNotEqual(a1, a2);

    // 1111 - matchingSamples=1, matchingFile=1, matchingExperiment=1,  matchingSelf=1
    a1.setExperiment(v1);
    a1.setFile(fileGroup1);
    a1.setSample(compositeGroup1);

    a1_same.setExperiment(v1);
    a1_same.setFile(fileGroup1);
    a1_same.setSample(compositeGroup1);
    assertEntitiesEqual(a1, a1_same, true);

    a1.setInfo("key1", "f5c9381090a53c54358feb2ba5b7a3d7");
    a1_same.setInfo("key2", "6329334b-dcd5-53c8-98fd-9812ac386d30");
    assertEntitiesNotEqual(a1, a1_same);
  }

  @Test
  public void testSequencingReadAnalysis(){
    val seq1 = SequencingRead.builder()
        .analysisId("a1")
        .aligned(true)
        .alignmentTool("b1")
        .insertSize(99999L)
        .libraryStrategy(LIBRARY_STRATEGIES.get(0))
        .pairedEnd(false)
        .referenceGenome("c1")
        .build();

    val seq2 = SequencingRead.builder()
        .analysisId("a2")
        .aligned(false)
        .alignmentTool("b2")
        .insertSize(55555L)
        .libraryStrategy(LIBRARY_STRATEGIES.get(1))
        .pairedEnd(false)
        .referenceGenome("c2")
        .build();

    val donor1 = Donor.builder()
        .donorId("myDonor1")
        .donorSubmitterId("myDonorSubmitter1")
        .studyId(DEFAULT_STUDY_ID)
        .donorGender("male")
        .build();
    val donor2 = Donor.builder()
        .donorId("myDonor2")
        .donorSubmitterId("myDonorSubmitter2")
        .studyId(DEFAULT_STUDY_ID)
        .donorGender("female")
        .build();

    val specimen1 = Specimen.builder()
        .specimenId("mySpecimen1")
        .specimenSubmitterId("mySpecimenSubmitter1")
        .donorId("myDonor1")
        .specimenClass(SPECIMEN_CLASSES.get(2))
        .specimenType(SPECIMEN_TYPES.get(2))
        .build();

    val specimen2 = Specimen.builder()
        .specimenId("mySpecimen2")
        .specimenSubmitterId("mySpecimenSubmitter2")
        .donorId("myDonor2")
        .specimenClass(SPECIMEN_CLASSES.get(1))
        .specimenType(SPECIMEN_TYPES.get(1))
        .build();

    val sample1 = Sample.builder()
        .sampleId("mySample1")
        .sampleSubmitterId("mySubmitterSample1")
        .specimenId("mySpecimen1")
        .sampleType(SAMPLE_TYPES.get(2))
        .build();

    val sample2 = Sample.builder()
        .sampleId("mySample2")
        .sampleSubmitterId("mySubmitterSample2")
        .specimenId("mySpecimen2")
        .sampleType(SAMPLE_TYPES.get(3))
        .build();

    val compositeEntity11 = CompositeEntity.create(sample1);
    compositeEntity11.setDonor(donor1);
    compositeEntity11.setSpecimen(specimen2);

    val compositeEntity12 = CompositeEntity.create(sample1);
    compositeEntity12.setDonor(donor2);
    compositeEntity12.setSpecimen(specimen1);

    val compositeGroup1 = newArrayList(compositeEntity11, compositeEntity12);

    val compositeEntity21 = CompositeEntity.create(sample2);
    compositeEntity21.setDonor(donor1);
    compositeEntity21.setSpecimen(specimen2);

    val compositeEntity22 = CompositeEntity.create(sample2);
    compositeEntity22.setDonor(donor2);
    compositeEntity22.setSpecimen(specimen1);

    val compositeGroup2 = newArrayList(compositeEntity21, compositeEntity22);

    val file11 = FileEntity.builder()
        .objectId("d11")
        .analysisId("a11")
        .fileName("c11")
        .studyId("e11")
        .fileSize(113L)
        .fileType(FILE_TYPES.get(0))
        .fileMd5sum("b11")
        .fileAccess(CONTROLLED.toString())
        .build();

    val file12 = FileEntity.builder()
        .objectId("d12")
        .analysisId("a12")
        .fileName("c12")
        .studyId("e12")
        .fileSize(114L)
        .fileType(FILE_TYPES.get(0))
        .fileMd5sum("b12")
        .fileAccess(CONTROLLED.toString())
        .build();

    val fileGroup1 = newArrayList(file11, file12);

    val file21 = FileEntity.builder()
        .objectId("d21")
        .analysisId("a21")
        .fileName("c21")
        .studyId("e21")
        .fileSize(213L)
        .fileType(FILE_TYPES.get(1))
        .fileMd5sum("b21")
        .fileAccess(CONTROLLED.toString())
        .build();

    val file22 = FileEntity.builder()
        .objectId("d22")
        .analysisId("a22")
        .fileName("c22")
        .studyId("e22")
        .fileSize(214L)
        .fileType(FILE_TYPES.get(1))
        .fileMd5sum("b22")
        .fileAccess(CONTROLLED.toString())
        .build();

    val fileGroup2 = newArrayList(file21, file22);

    val a1 = new SequencingReadAnalysis();
    a1.setAnalysisId("a1");
    a1.setAnalysisState(PUBLISHED.toString());
    a1.setStudy("b1");

    val a1_same = new SequencingReadAnalysis();
    a1_same.setAnalysisId("a1");
    a1_same.setAnalysisState(PUBLISHED.toString());
    a1_same.setStudy("b1");

    assertEntitiesEqual(a1, a1_same, true);

    val a2 = new SequencingReadAnalysis();
    a2.setAnalysisId("a2");
    a2.setAnalysisState(UNPUBLISHED.toString());
    a2.setStudy("b2");

    val a2_same = new SequencingReadAnalysis();
    a2_same.setAnalysisId("a2");
    a2_same.setAnalysisState(UNPUBLISHED.toString());
    a2_same.setStudy("b2");

    assertEntitiesEqual(a2, a2_same, true);

    // 0000 - matchingSamples=0, matchingFile=0, matchingExperiment=0,  matchingSelf=0
    a1.setExperiment(seq1);
    a1.setFile(fileGroup1);
    a1.setSample(compositeGroup1);

    a2.setExperiment(seq2);
    a2.setFile(fileGroup2);
    a2.setSample(compositeGroup2);
    assertEntitiesNotEqual(a1, a2);

    // 0001 - matchingSamples=0, matchingFile=0, matchingExperiment=0,  matchingSelf=1
    a1.setExperiment(seq1);
    a1.setFile(fileGroup1);
    a1.setSample(compositeGroup1);

    a1_same.setExperiment(seq2);
    a1_same.setFile(fileGroup2);
    a1_same.setSample(compositeGroup2);
    assertEntitiesNotEqual(a1, a2);

    // 0010 - matchingSamples=0, matchingFile=0, matchingExperiment=1,  matchingSelf=0
    a1.setExperiment(seq1);
    a1.setFile(fileGroup1);
    a1.setSample(compositeGroup1);

    a2.setExperiment(seq1);
    a2.setFile(fileGroup2);
    a2.setSample(compositeGroup2);
    assertEntitiesNotEqual(a1, a2);

    // 0011 - matchingSamples=0, matchingFile=0, matchingExperiment=1,  matchingSelf=1
    a1.setExperiment(seq1);
    a1.setFile(fileGroup1);
    a1.setSample(compositeGroup1);

    a1_same.setExperiment(seq1);
    a1_same.setFile(fileGroup2);
    a1_same.setSample(compositeGroup2);
    assertEntitiesNotEqual(a1, a2);

    // 0100 - matchingSamples=0, matchingFile=1, matchingExperiment=0,  matchingSelf=0
    a1.setExperiment(seq1);
    a1.setFile(fileGroup1);
    a1.setSample(compositeGroup1);

    a2.setExperiment(seq2);
    a2.setFile(fileGroup1);
    a2.setSample(compositeGroup2);
    assertEntitiesNotEqual(a1, a2);

    // 0101 - matchingSamples=0, matchingFile=1, matchingExperiment=0,  matchingSelf=1
    a1.setExperiment(seq1);
    a1.setFile(fileGroup1);
    a1.setSample(compositeGroup1);

    a1_same.setExperiment(seq2);
    a1_same.setFile(fileGroup1);
    a1_same.setSample(compositeGroup2);
    assertEntitiesNotEqual(a1, a2);

    // 0110 - matchingSamples=0, matchingFile=1, matchingExperiment=1,  matchingSelf=0
    a1.setExperiment(seq1);
    a1.setFile(fileGroup1);
    a1.setSample(compositeGroup1);

    a2.setExperiment(seq1);
    a2.setFile(fileGroup1);
    a2.setSample(compositeGroup2);
    assertEntitiesNotEqual(a1, a2);

    // 0111 - matchingSamples=0, matchingFile=1, matchingExperiment=1,  matchingSelf=1
    a1.setExperiment(seq1);
    a1.setFile(fileGroup1);
    a1.setSample(compositeGroup1);

    a1_same.setExperiment(seq1);
    a1_same.setFile(fileGroup1);
    a1_same.setSample(compositeGroup2);
    assertEntitiesNotEqual(a1, a2);

    // 1000 - matchingSamples=1, matchingFile=0, matchingExperiment=0,  matchingSelf=0
    a1.setExperiment(seq1);
    a1.setFile(fileGroup1);
    a1.setSample(compositeGroup1);

    a2.setExperiment(seq2);
    a2.setFile(fileGroup2);
    a2.setSample(compositeGroup1);
    assertEntitiesNotEqual(a1, a2);

    // 1001 - matchingSamples=1, matchingFile=0, matchingExperiment=0,  matchingSelf=1
    a1.setExperiment(seq1);
    a1.setFile(fileGroup1);
    a1.setSample(compositeGroup1);

    a1_same.setExperiment(seq2);
    a1_same.setFile(fileGroup2);
    a1_same.setSample(compositeGroup1);
    assertEntitiesNotEqual(a1, a2);

    // 1010 - matchingSamples=1, matchingFile=0, matchingExperiment=1,  matchingSelf=0
    a1.setExperiment(seq1);
    a1.setFile(fileGroup1);
    a1.setSample(compositeGroup1);

    a2.setExperiment(seq1);
    a2.setFile(fileGroup2);
    a2.setSample(compositeGroup1);
    assertEntitiesNotEqual(a1, a2);

    // 1011 - matchingSamples=1, matchingFile=0, matchingExperiment=1,  matchingSelf=1
    a1.setExperiment(seq1);
    a1.setFile(fileGroup1);
    a1.setSample(compositeGroup1);

    a1_same.setExperiment(seq1);
    a1_same.setFile(fileGroup2);
    a1_same.setSample(compositeGroup1);
    assertEntitiesNotEqual(a1, a2);

    // 1100 - matchingSamples=1, matchingFile=1, matchingExperiment=0,  matchingSelf=0
    a1.setExperiment(seq1);
    a1.setFile(fileGroup1);
    a1.setSample(compositeGroup1);

    a2.setExperiment(seq2);
    a2.setFile(fileGroup1);
    a2.setSample(compositeGroup1);
    assertEntitiesNotEqual(a1, a2);

    // 1101 - matchingSamples=1, matchingFile=1, matchingExperiment=0,  matchingSelf=1
    a1.setExperiment(seq1);
    a1.setFile(fileGroup1);
    a1.setSample(compositeGroup1);

    a1_same.setExperiment(seq2);
    a1_same.setFile(fileGroup1);
    a1_same.setSample(compositeGroup1);
    assertEntitiesNotEqual(a1, a2);

    // 1110 - matchingSamples=1, matchingFile=1, matchingExperiment=1,  matchingSelf=0
    a1.setExperiment(seq1);
    a1.setFile(fileGroup1);
    a1.setSample(compositeGroup1);

    a2.setExperiment(seq1);
    a2.setFile(fileGroup1);
    a2.setSample(compositeGroup1);
    assertEntitiesNotEqual(a1, a2);

    // 1111 - matchingSamples=1, matchingFile=1, matchingExperiment=1,  matchingSelf=1
    a1.setExperiment(seq1);
    a1.setFile(fileGroup1);
    a1.setSample(compositeGroup1);

    a1_same.setExperiment(seq1);
    a1_same.setFile(fileGroup1);
    a1_same.setSample(compositeGroup1);
    assertEntitiesEqual(a1, a1_same, true);

    a1.setInfo("key1", "f5c9381090a53c54358feb2ba5b7a3d7");
    a1_same.setInfo("key2", "6329334b-dcd5-53c8-98fd-9812ac386d30");
    assertEntitiesNotEqual(a1, a1_same);
  }

  @Test
  public void testSequencingRead(){
    val s1 = new SequencingRead();
    s1.setAnalysisId("a1");
    s1.setAligned(true);
    s1.setAlignmentTool("b1");
    s1.setInsertSize(99999L);
    s1.setLibraryStrategy(LIBRARY_STRATEGIES.get(0));
    s1.setPairedEnd(false);
    s1.setReferenceGenome("c1");

    val s1_same = SequencingRead.builder()
        .analysisId("a1")
        .aligned(true)
        .alignmentTool("b1")
        .insertSize(99999L)
        .libraryStrategy(LIBRARY_STRATEGIES.get(0))
        .pairedEnd(false)
        .referenceGenome("c1")
        .build();

    assertEntitiesEqual(s1, s1_same, true);

    val s2 = SequencingRead.builder()
        .analysisId("a2")
        .aligned(false)
        .alignmentTool("b2")
        .insertSize(55555L)
        .libraryStrategy(LIBRARY_STRATEGIES.get(1))
        .pairedEnd(false)
        .referenceGenome("c2")
        .build();
    assertEntitiesNotEqual(s1,s2);

    s1.setInfo("key1", "f5c9381090a53c54358feb2ba5b7a3d7");
    s1_same.setInfo("key2", "6329334b-dcd5-53c8-98fd-9812ac386d30");
    assertEntitiesNotEqual(s1,s1_same);

    // Test getters
    assertThat(s1.getAligned()).isTrue();
    assertThat(s1.getAlignmentTool()).isEqualTo("b1");
    assertThat(s1.getAnalysisId()).isEqualTo("a1");
    assertThat(s1.getInsertSize()).isEqualTo(99999L);
    assertThat(s1.getLibraryStrategy()).isEqualTo(LIBRARY_STRATEGIES.get(0));
    assertThat(s1.getPairedEnd()).isFalse();
    assertThat(s1.getReferenceGenome()).isEqualTo("c1");
    assertInfoKVPair(s1, "key1", "f5c9381090a53c54358feb2ba5b7a3d7");
  }

  @Test
  public void testVariantCall(){
    val v1 = new VariantCall();
    v1.setAnalysisId("a1");
    v1.setMatchedNormalSampleSubmitterId("b1");
    v1.setVariantCallingTool("c1");

    val v1_same = VariantCall.builder()
        .analysisId("a1")
        .variantCallingTool("c1")
        .matchedNormalSampleSubmitterId("b1")
        .build();

    assertEntitiesEqual(v1, v1_same, true);

    val v2 = VariantCall.builder()
        .analysisId("a2")
        .variantCallingTool("c2")
        .matchedNormalSampleSubmitterId("b2")
        .build();
    assertEntitiesNotEqual(v1, v2);

    v1.setInfo("key1", "f5c9381090a53c54358feb2ba5b7a3d7");
    v1_same.setInfo("key2", "6329334b-dcd5-53c8-98fd-9812ac386d30");
    assertEntitiesNotEqual(v1,v1_same);

    /// Test getters
    assertThat(v1.getAnalysisId()).isEqualTo("a1");
    assertThat(v1.getMatchedNormalSampleSubmitterId()).isEqualTo("b1");
    assertThat(v1.getVariantCallingTool()).isEqualTo("c1");
    assertInfoKVPair(v1, "key1", "f5c9381090a53c54358feb2ba5b7a3d7");
  }

  @Test
  public void testDonor(){
    val donor1 = new Donor();
    donor1.setDonorGender("male");
    donor1.setDonorSubmitterId("myDonorSubmitter1");
    donor1.setDonorId("myDonor1");
    donor1.setStudyId("study1");

    val donor1_same = Donor.builder()
        .donorId("myDonor1")
        .donorSubmitterId("myDonorSubmitter1")
        .studyId("study1")
        .donorGender("male")
        .build();
    assertEntitiesEqual(donor1, donor1_same, true);

    val donor2 = Donor.builder()
        .donorId("myDonor2")
        .donorSubmitterId("myDonorSubmitter2")
        .studyId("study2")
        .donorGender("female")
        .build();
    assertEntitiesNotEqual(donor1, donor2);

    donor1.setInfo("key1", "f5c9381090a53c54358feb2ba5b7a3d7");
    donor1_same.setInfo("key2", "6329334b-dcd5-53c8-98fd-9812ac386d30");
    assertEntitiesNotEqual(donor1, donor1_same);

    // Test getters
    assertThat(donor1.getStudyId()).isEqualTo("study1");
    assertThat(donor1.getDonorGender()).isEqualTo("male");
    assertThat(donor1.getDonorSubmitterId()).isEqualTo("myDonorSubmitter1");
    assertThat(donor1.getDonorId()).isEqualTo("myDonor1");
    assertInfoKVPair(donor1, "key1", "f5c9381090a53c54358feb2ba5b7a3d7");
  }

  @Test
  public void testStudy(){
    val study1 = new Study();
    study1.setDescription("a");
    study1.setName("b");
    study1.setOrganization("c");
    study1.setStudyId("d");

    val study1_same = Study.builder()
        .studyId("d")
        .name("b")
        .organization("c")
        .description("a")
        .build();

    assertEntitiesEqual(study1, study1_same, true);

    val study2 = Study.builder()
        .studyId("d1")
        .name("b1")
        .organization("c1")
        .description("a1")
        .build();
    assertEntitiesNotEqual(study1, study2);

    study1.setInfo("key1", "f5c9381090a53c54358feb2ba5b7a3d7");
    study1_same.setInfo("key2", "6329334b-dcd5-53c8-98fd-9812ac386d30");
    assertEntitiesNotEqual(study1, study1_same);

    // Test getters
    assertThat(study1.getDescription()).isEqualTo("a");
    assertThat(study1.getName()).isEqualTo("b");
    assertThat(study1.getOrganization()).isEqualTo("c");
    assertThat(study1.getStudyId()).isEqualTo("d");
    assertInfoKVPair(study1, "key1", "f5c9381090a53c54358feb2ba5b7a3d7");
  }

  @Test
  public void testSpecimen(){
    val s1 = new Specimen();
    s1.setDonorId("a1");
    s1.setSpecimenClass(SPECIMEN_CLASSES.get(0));
    s1.setSpecimenId("b1");
    s1.setSpecimenSubmitterId("c1");
    s1.setSpecimenType(SPECIMEN_TYPES.get(0));

    val s1_same = Specimen.builder()
        .specimenId("b1")
        .specimenSubmitterId("c1")
        .donorId("a1")
        .specimenClass(SPECIMEN_CLASSES.get(0))
        .specimenType(SPECIMEN_TYPES.get(0))
        .build();

    assertEntitiesEqual(s1, s1_same, true);

    val s2 = Specimen.builder()
        .specimenId("b2")
        .specimenSubmitterId("c2")
        .donorId("a2")
        .specimenClass(SPECIMEN_CLASSES.get(1))
        .specimenType(SPECIMEN_TYPES.get(1))
        .build();
    assertEntitiesNotEqual(s1, s2);

    s1.setInfo("key1", "f5c9381090a53c54358feb2ba5b7a3d7");
    s1_same.setInfo("key2", "6329334b-dcd5-53c8-98fd-9812ac386d30");
    assertEntitiesNotEqual(s1, s1_same);

    // Test getters
    assertThat(s1.getSpecimenType()).isEqualTo(SPECIMEN_TYPES.get(0));
    assertThat(s1.getSpecimenClass()).isEqualTo(SPECIMEN_CLASSES.get(0));
    assertThat(s1.getSpecimenSubmitterId()).isEqualTo("c1");
    assertThat(s1.getSpecimenId()).isEqualTo("b1");
    assertThat(s1.getDonorId()).isEqualTo("a1");
    assertInfoKVPair(s1, "key1", "f5c9381090a53c54358feb2ba5b7a3d7");
  }

  @Test
  public void testAnalysisStates(){
    assertThat(resolveAnalysisState("PUBLISHED")).isEqualByComparingTo(PUBLISHED);
    assertThat(resolveAnalysisState("UNPUBLISHED")).isEqualByComparingTo(UNPUBLISHED);
    assertThat(resolveAnalysisState("SUPPRESSED")).isEqualByComparingTo(SUPPRESSED);
    val erroredStates = newArrayList("published", "unpublished", "suppressed", "anything");
    for (val state : erroredStates){
      val thrown = catchThrowable(() -> resolveAnalysisState(state));
      assertThat(thrown).isInstanceOf(IllegalStateException.class);
    }
  }

  private static void assertEntitiesEqual(Object actual, Object expected, boolean checkFieldByField){
    if (checkFieldByField){
      assertThat(actual).isEqualToComparingFieldByField(expected);
    }
    assertThat(actual).isEqualTo(expected);
    assertThat(actual.hashCode()).isEqualTo(expected.hashCode());
  }

  private static void assertEntitiesNotEqual(Object actual, Object expected){
    assertThat(actual).isNotEqualTo(expected);
    assertThat(actual.hashCode()).isNotEqualTo(expected.hashCode());
  }

}
