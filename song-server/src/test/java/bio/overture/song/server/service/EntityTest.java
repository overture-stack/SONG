/*
 * Copyright (c) 2019. Ontario Institute for Cancer Research
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

import static bio.overture.song.core.model.enums.AccessTypes.CONTROLLED;
import static bio.overture.song.core.model.enums.AnalysisStates.PUBLISHED;
import static bio.overture.song.core.model.enums.AnalysisStates.SUPPRESSED;
import static bio.overture.song.core.model.enums.AnalysisStates.UNPUBLISHED;
import static bio.overture.song.core.model.enums.AnalysisStates.resolveAnalysisState;
import static bio.overture.song.core.testing.SongErrorAssertions.assertExceptionThrownBy;
import static bio.overture.song.server.utils.TestConstants.SAMPLE_TYPE;
import static bio.overture.song.server.utils.TestConstants.SPECIMEN_TISSUE_SOURCE;
import static bio.overture.song.server.utils.TestConstants.SPECIMEN_TYPE;
import static bio.overture.song.server.utils.TestConstants.TUMOUR_NORMAL_DESIGNATION;
import static bio.overture.song.server.utils.TestFiles.assertInfoKVPair;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import bio.overture.song.core.model.Metadata;
import bio.overture.song.core.model.enums.FileTypes;
import bio.overture.song.server.model.Upload;
import bio.overture.song.server.model.entity.Donor;
import bio.overture.song.server.model.entity.FileEntity;
import bio.overture.song.server.model.entity.Sample;
import bio.overture.song.server.model.entity.Specimen;
import bio.overture.song.server.model.entity.Study;
import bio.overture.song.server.model.entity.composites.CompositeEntity;
import bio.overture.song.server.model.entity.composites.DonorWithSpecimens;
import bio.overture.song.server.model.entity.composites.SpecimenWithSamples;
import bio.overture.song.server.model.entity.composites.StudyWithDonors;
import bio.overture.song.server.model.enums.UploadStates;
import bio.overture.song.server.model.legacy.LegacyEntity;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.List;
import lombok.val;
import org.junit.Test;

public class EntityTest {
  private static final String DEFAULT_STUDY_ID = "ABC123";
  private static final List<String> SAMPLE_TYPES = newArrayList(SAMPLE_TYPE);
  private static final List<String> FILE_TYPES =
      stream(FileTypes.values()).map(FileTypes::toString).collect(toList());

  @Test
  public void testNullMetadata() {
    val m = new Metadata();
    m.setInfo((String) null);
    m.setInfo((JsonNode) null);
    m.addInfo(null);
  }

  @Test
  public void testCompositeEntity() {
    val donor1 =
        Donor.builder()
            .donorId("myDonor1")
            .submitterDonorId("myDonorSubmitter1")
            .studyId(DEFAULT_STUDY_ID)
            .gender("male")
            .build();
    val donor2 =
        Donor.builder()
            .donorId("myDonor2")
            .submitterDonorId("myDonorSubmitter2")
            .studyId(DEFAULT_STUDY_ID)
            .gender("female")
            .build();

    val specimen1 =
        Specimen.builder()
            .specimenId("mySpecimen1")
            .submitterSpecimenId("mySpecimenSubmitter1")
            .donorId("myDonor1")
            .tumourNormalDesignation(TUMOUR_NORMAL_DESIGNATION.get(1))
            .specimenTissueSource(SPECIMEN_TISSUE_SOURCE.get(2))
            .specimenType(SPECIMEN_TYPE.get(2))
            .build();

    val specimen2 =
        Specimen.builder()
            .specimenId("mySpecimen2")
            .submitterSpecimenId("mySpecimenSubmitter2")
            .donorId("myDonor2")
            .tumourNormalDesignation(TUMOUR_NORMAL_DESIGNATION.get(0))
            .specimenTissueSource(SPECIMEN_TISSUE_SOURCE.get(1))
            .specimenType(SPECIMEN_TYPE.get(1))
            .build();

    val sample1 =
        Sample.builder()
            .sampleId("mySample1")
            .submitterSampleId("mySubmitterSample1")
            .specimenId("mySpecimen1")
            .sampleType(SAMPLE_TYPES.get(1))
            .build();

    val sample2 =
        Sample.builder()
            .sampleId("mySample2")
            .submitterSampleId("mySubmitterSample2")
            .specimenId("mySpecimen2")
            .sampleType(SAMPLE_TYPES.get(3))
            .build();

    val compositeEntity1 = CompositeEntity.create(sample1);
    compositeEntity1.setDonor(donor1);
    compositeEntity1.setSpecimen(specimen1);

    val compositeEntity1_same = new CompositeEntity();
    compositeEntity1_same.setSampleType(SAMPLE_TYPES.get(1));
    compositeEntity1_same.setSubmitterSampleId("mySubmitterSample1");
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

    assertEquals(compositeEntity1.getSampleId(), "mySample1");
    assertEquals(compositeEntity1.getSampleType(), SAMPLE_TYPES.get(1));
    assertEquals(compositeEntity1.getSubmitterSampleId(), "mySubmitterSample1");
    assertEquals(compositeEntity1.getSpecimenId(), "mySpecimen1");
    assertEquals(compositeEntity1.getSpecimen(), specimen1);
    assertEquals(compositeEntity1.getDonor(), donor1);
    assertInfoKVPair(compositeEntity1, "key1", "f5c9381090a53c54358feb2ba5b7a3d7");
  }

  @Test
  public void testDonorWithSpecimens() {
    val specimen1 =
        Specimen.builder()
            .specimenId("mySpecimen1")
            .submitterSpecimenId("mySpecimenSubmitter1")
            .donorId("myDonor1")
            .tumourNormalDesignation(TUMOUR_NORMAL_DESIGNATION.get(0))
            .specimenTissueSource(SPECIMEN_TISSUE_SOURCE.get(2))
            .specimenType(SPECIMEN_TYPE.get(2))
            .build();

    val specimen2 =
        Specimen.builder()
            .specimenId("mySpecimen2")
            .submitterSpecimenId("mySpecimenSubmitter2")
            .donorId("myDonor2")
            .tumourNormalDesignation(TUMOUR_NORMAL_DESIGNATION.get(1))
            .specimenTissueSource(SPECIMEN_TISSUE_SOURCE.get(1))
            .specimenType(SPECIMEN_TYPE.get(1))
            .build();

    val sample11 =
        Sample.builder()
            .sampleId("mySample11")
            .submitterSampleId("mySubmitterSample11")
            .specimenId("mySpecimen11")
            .sampleType(SAMPLE_TYPES.get(2))
            .build();

    val sample12 =
        Sample.builder()
            .sampleId("mySample12")
            .submitterSampleId("mySubmitterSample12")
            .specimenId("mySpecimen12")
            .sampleType(SAMPLE_TYPES.get(2))
            .build();

    val sampleGroup1 = newArrayList(sample11, sample12);

    val sample21 =
        Sample.builder()
            .sampleId("mySample21")
            .submitterSampleId("mySubmitterSample21")
            .specimenId("mySpecimen21")
            .sampleType(SAMPLE_TYPES.get(3))
            .build();

    val sample22 =
        Sample.builder()
            .sampleId("mySample22")
            .submitterSampleId("mySubmitterSample22")
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

    val donor1 =
        Donor.builder()
            .donorId("myDonor1")
            .submitterDonorId("myDonorSubmitter1")
            .studyId(DEFAULT_STUDY_ID)
            .gender("male")
            .build();
    val donor2 =
        Donor.builder()
            .donorId("myDonor2")
            .submitterDonorId("myDonorSubmitter2")
            .studyId(DEFAULT_STUDY_ID)
            .gender("female")
            .build();

    val d1 = new DonorWithSpecimens();
    d1.setGender(donor1.getGender());
    d1.setDonorId(donor1.getDonorId());
    d1.setSubmitterDonorId(donor1.getSubmitterDonorId());
    d1.setStudyId(donor1.getStudyId());

    val d2 = new DonorWithSpecimens();
    d2.setDonor(donor1);

    assertEntitiesEqual(d1, d2, true);

    // 00 - matchingDonors=0   matchingSpecimenGroups=0
    d1.setDonor(donor1);
    d1.setSpecimens(specimenWSampleGroup1);
    d2.setDonor(donor2);
    d2.setSpecimens(specimenWSampleGroup2);
    assertEntitiesNotEqual(d1, d2);

    // 01 - matchingDonors=0   matchingSpecimenGroups=1
    d1.setDonor(donor1);
    d1.setSpecimens(specimenWSampleGroup1);
    d2.setDonor(donor2);
    d2.setSpecimens(specimenWSampleGroup1);
    assertEntitiesNotEqual(d1, d2);

    // 10 - matchingDonors=1   matchingSpecimenGroups=0
    d1.setDonor(donor1);
    d1.setSpecimens(specimenWSampleGroup1);
    d2.setDonor(donor1);
    d2.setSpecimens(specimenWSampleGroup2);
    assertEntitiesNotEqual(d1, d2);

    // 11 - matchingDonors=1   matchingSpecimenGroups=1
    d1.setDonor(donor1);
    d1.setSpecimens(specimenWSampleGroup1);
    d2.setDonor(donor1);
    d2.setSpecimens(specimenWSampleGroup1);
    assertEntitiesEqual(d1, d2, true);

    d1.setInfo("key1", "f5c9381090a53c54358feb2ba5b7a3d7");
    d2.setInfo("key2", "6329334b-dcd5-53c8-98fd-9812ac386d30");
    assertEntitiesNotEqual(d1, d2);

    // Test getters
    assertEquals(d1.getGender(), donor1.getGender());
    assertEquals(d1.getSubmitterDonorId(), donor1.getSubmitterDonorId());
    assertEquals(d1.getDonorId(), donor1.getDonorId());
    assertEquals(d1.getStudyId(), donor1.getStudyId());
    assertThat(d1.getSpecimens(), containsInAnyOrder(specimenWithSample1, specimenWithSample2));
    assertNotEquals(d1.createDonor(), donor1);
    assertInfoKVPair(d1, "key1", "f5c9381090a53c54358feb2ba5b7a3d7");
  }

  @Test
  public void testSpecimenWithSamples() {
    val specimen1 =
        Specimen.builder()
            .specimenId("mySpecimen1")
            .submitterSpecimenId("mySpecimenSubmitter1")
            .donorId("myDonor1")
            .tumourNormalDesignation(TUMOUR_NORMAL_DESIGNATION.get(0))
            .specimenTissueSource(SPECIMEN_TISSUE_SOURCE.get(2))
            .specimenType(SPECIMEN_TYPE.get(2))
            .build();

    val specimen2 =
        Specimen.builder()
            .specimenId("mySpecimen2")
            .submitterSpecimenId("mySpecimenSubmitter2")
            .donorId("myDonor2")
            .tumourNormalDesignation(TUMOUR_NORMAL_DESIGNATION.get(1))
            .specimenTissueSource(SPECIMEN_TISSUE_SOURCE.get(1))
            .specimenType(SPECIMEN_TYPE.get(1))
            .build();

    val sample11 =
        Sample.builder()
            .sampleId("mySample11")
            .submitterSampleId("mySubmitterSample11")
            .specimenId("mySpecimen11")
            .sampleType(SAMPLE_TYPES.get(2))
            .build();

    val sample12 =
        Sample.builder()
            .sampleId("mySample12")
            .submitterSampleId("mySubmitterSample12")
            .specimenId("mySpecimen12")
            .sampleType(SAMPLE_TYPES.get(2))
            .build();

    val sampleGroup1 = newArrayList(sample11, sample12);

    val sample21 =
        Sample.builder()
            .sampleId("mySample21")
            .submitterSampleId("mySubmitterSample21")
            .specimenId("mySpecimen21")
            .sampleType(SAMPLE_TYPES.get(3))
            .build();

    val sample22 =
        Sample.builder()
            .sampleId("mySample22")
            .submitterSampleId("mySubmitterSample22")
            .specimenId("mySpecimen22")
            .sampleType(SAMPLE_TYPES.get(3))
            .build();

    val sampleGroup2 = newArrayList(sample21, sample22);

    val s1 = new SpecimenWithSamples();
    s1.setDonorId(specimen1.getDonorId());
    s1.setTumourNormalDesignation(specimen1.getTumourNormalDesignation());
    s1.setSpecimenType(specimen1.getSpecimenType());
    s1.setSpecimenTissueSource(specimen1.getSpecimenTissueSource());
    s1.setSubmitterSpecimenId(specimen1.getSubmitterSpecimenId());
    s1.setSpecimenId(specimen1.getSpecimenId());

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
    assertEntitiesNotEqual(s1, s2);

    // Test getters
    assertEquals(s1.getDonorId(), specimen1.getDonorId());
    assertEquals(s1.getTumourNormalDesignation(), specimen1.getTumourNormalDesignation());
    assertEquals(s1.getSpecimenType(), specimen1.getSpecimenType());
    assertEquals(s1.getSpecimenTissueSource(), specimen1.getSpecimenTissueSource());
    assertEquals(s1.getSubmitterSpecimenId(), specimen1.getSubmitterSpecimenId());
    assertEquals(s1.getSpecimenId(), specimen1.getSpecimenId());
    assertThat(s1.getSamples(), containsInAnyOrder(sample11, sample12));
    assertInfoKVPair(s1, "key1", "f5c9381090a53c54358feb2ba5b7a3d7");

    // Test addSample
    val sLeft = new SpecimenWithSamples();
    sLeft.setDonorId(specimen1.getDonorId());
    sLeft.setTumourNormalDesignation(specimen1.getTumourNormalDesignation());
    sLeft.setSpecimenType(specimen1.getSpecimenType());
    sLeft.setSpecimenTissueSource(specimen1.getSpecimenTissueSource());
    sLeft.setSubmitterSpecimenId(specimen1.getSubmitterSpecimenId());
    sLeft.setSpecimenId(specimen1.getSpecimenId());
    sLeft.setSamples(sampleGroup2);

    val sRight = new SpecimenWithSamples();
    sRight.setDonorId(specimen1.getDonorId());
    sRight.setTumourNormalDesignation(specimen1.getTumourNormalDesignation());
    sRight.setSpecimenType(specimen1.getSpecimenType());
    sRight.setSpecimenTissueSource(specimen1.getSpecimenTissueSource());
    sRight.setSubmitterSpecimenId(specimen1.getSubmitterSpecimenId());
    sRight.setSpecimenId(specimen1.getSpecimenId());
    sampleGroup2.forEach(sRight::addSample);

    assertEquals(sLeft, sRight);
  }

  @Test
  public void testStudyWithDonors() {
    val study1 =
        Study.builder().studyId("d1").name("b1").organization("c1").description("a1").build();

    val study2 =
        Study.builder().studyId("d2").name("b2").organization("c2").description("a2").build();

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
    assertEntitiesNotEqual(s1, s2);

    // ---------------
    val specimen1 =
        Specimen.builder()
            .specimenId("mySpecimen1")
            .submitterSpecimenId("mySpecimenSubmitter1")
            .donorId("myDonor1")
            .tumourNormalDesignation(TUMOUR_NORMAL_DESIGNATION.get(0))
            .specimenTissueSource(SPECIMEN_TISSUE_SOURCE.get(2))
            .specimenType(SPECIMEN_TYPE.get(2))
            .build();

    val specimen2 =
        Specimen.builder()
            .specimenId("mySpecimen2")
            .submitterSpecimenId("mySpecimenSubmitter2")
            .donorId("myDonor2")
            .tumourNormalDesignation(TUMOUR_NORMAL_DESIGNATION.get(1))
            .specimenTissueSource(SPECIMEN_TISSUE_SOURCE.get(1))
            .specimenType(SPECIMEN_TYPE.get(1))
            .build();

    val sample11 =
        Sample.builder()
            .sampleId("mySample11")
            .submitterSampleId("mySubmitterSample11")
            .specimenId("mySpecimen11")
            .sampleType(SAMPLE_TYPES.get(2))
            .build();

    val sample12 =
        Sample.builder()
            .sampleId("mySample12")
            .submitterSampleId("mySubmitterSample12")
            .specimenId("mySpecimen12")
            .sampleType(SAMPLE_TYPES.get(2))
            .build();

    val sampleGroup1 = newArrayList(sample11, sample12);

    val sample21 =
        Sample.builder()
            .sampleId("mySample21")
            .submitterSampleId("mySubmitterSample21")
            .specimenId("mySpecimen21")
            .sampleType(SAMPLE_TYPES.get(3))
            .build();

    val sample22 =
        Sample.builder()
            .sampleId("mySample22")
            .submitterSampleId("mySubmitterSample22")
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

    val donor1 =
        Donor.builder()
            .donorId("myDonor1")
            .submitterDonorId("myDonorSubmitter1")
            .studyId(DEFAULT_STUDY_ID)
            .gender("male")
            .build();
    val donor2 =
        Donor.builder()
            .donorId("myDonor2")
            .submitterDonorId("myDonorSubmitter2")
            .studyId(DEFAULT_STUDY_ID)
            .gender("female")
            .build();

    val d1 = new DonorWithSpecimens();
    d1.setDonor(donor1);
    d1.setSpecimens(specimenWSampleGroup1);

    val d2 = new DonorWithSpecimens();
    d2.setDonor(donor2);
    d2.setSpecimens(specimenWSampleGroup2);

    // 00 -- matchingDonorGroup=0    matchingStudy=0
    s1.setStudy(study1);
    s1.setDonors(newArrayList(d1));
    s2.setStudy(study2);
    s2.setDonors(newArrayList(d2));
    assertEntitiesNotEqual(s1, s2);

    // 01 -- matchingDonorGroup=0    matchingStudy=1
    s1.setStudy(study1);
    s1.setDonors(newArrayList(d1));
    s2.setStudy(study1);
    s2.setDonors(newArrayList(d2));
    assertEntitiesNotEqual(s1, s2);

    // 10 -- matchingDonorGroup=1    matchingStudy=0
    s1.setStudy(study1);
    s1.setDonors(newArrayList(d1));
    s2.setStudy(study2);
    s2.setDonors(newArrayList(d1));
    assertEntitiesNotEqual(s1, s2);

    // 11 -- matchingDonorGroup=1    matchingStudy=1
    s1.setStudy(study1);
    s1.setDonors(newArrayList(d1));
    s2.setStudy(study1);
    s2.setDonors(newArrayList(d1));
    assertEntitiesEqual(s1, s2, true);

    s1.setInfo("key1", "f5c9381090a53c54358feb2ba5b7a3d7");
    s2.setInfo("key2", "6329334b-dcd5-53c8-98fd-9812ac386d30");
    assertInfoKVPair(s1, "key1", "f5c9381090a53c54358feb2ba5b7a3d7");
    assertEntitiesNotEqual(s1, s2);

    s1.setInfo("key1", "f5c9381090a53c54358feb2ba5b7a3d7");
    s2.setInfo("key1", "6329334b-dcd5-53c8-98fd-9812ac386d30");
    assertEntitiesNotEqual(s1, s2);

    // Test getters
    assertEquals(s1.getDescription(), study1.getDescription());
    assertEquals(s1.getName(), study1.getName());
    assertEquals(s1.getOrganization(), study1.getOrganization());
    assertEquals(s1.getStudyId(), study1.getStudyId());
    assertNotEquals(s1.getStudy(), study1);
    assertThat(s1.getDonors(), containsInAnyOrder(d1));
    assertInfoKVPair(s1, "key1", "f5c9381090a53c54358feb2ba5b7a3d7");

    // Assert addDonor
    val sLeft = new StudyWithDonors();
    sLeft.setStudy(study1);
    sLeft.setDonors(newArrayList(d1, d2));

    val sRight = new StudyWithDonors();
    sRight.setStudy(study1);
    sRight.addDonor(d1);
    sRight.addDonor(d2);
    assertEquals(sLeft, sRight);
  }

  @Test
  public void testFile() {
    val file1 = new FileEntity();
    file1.setAnalysisId("a1");
    file1.setFileAccess(CONTROLLED);
    file1.setFileMd5sum("b1");
    file1.setFileName("c1");
    file1.setFileSize(13L);
    file1.setFileType(FILE_TYPES.get(0));
    file1.setObjectId("d1");
    file1.setStudyId("e1");

    val file1_same =
        FileEntity.builder()
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

    val file2 =
        FileEntity.builder()
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
    assertEquals(file1.getAnalysisId(), "a1");
    assertEquals(file1.getFileAccess(), CONTROLLED.toString());
    assertEquals(file1.getFileMd5sum(), "b1");
    assertEquals(file1.getFileName(), "c1");
    assertEquals(file1.getFileType(), FILE_TYPES.get(0));
    assertEquals(file1.getObjectId(), "d1");
    assertEquals(file1.getStudyId(), "e1");
    assertInfoKVPair(file1, "key1", "f5c9381090a53c54358feb2ba5b7a3d7");
  }

  @Test
  public void testSample() {
    val sample1 = new Sample();
    sample1.setSampleId("a1");
    sample1.setSubmitterSampleId("b1");
    sample1.setSampleType(SAMPLE_TYPES.get(0));
    sample1.setSpecimenId("c1");

    val sample1_same =
        Sample.builder()
            .sampleId("a1")
            .submitterSampleId("b1")
            .sampleType(SAMPLE_TYPES.get(0))
            .specimenId("c1")
            .build();
    assertEntitiesEqual(sample1, sample1_same, true);

    val sample2 =
        Sample.builder()
            .sampleId("a2")
            .submitterSampleId("b2")
            .sampleType(SAMPLE_TYPES.get(1))
            .specimenId("c2")
            .build();
    assertEntitiesNotEqual(sample1, sample2);

    sample1.setInfo("key1", "f5c9381090a53c54358feb2ba5b7a3d7");
    sample1_same.setInfo("key2", "6329334b-dcd5-53c8-98fd-9812ac386d30");
    assertEntitiesNotEqual(sample1, sample1_same);

    // Test getters
    assertEquals(sample1.getSampleId(), "a1");
    assertEquals(sample1.getSubmitterSampleId(), "b1");
    assertEquals(sample1.getSampleType(), SAMPLE_TYPES.get(0));
    assertEquals(sample1.getSpecimenId(), "c1");
    assertInfoKVPair(sample1, "key1", "f5c9381090a53c54358feb2ba5b7a3d7");
  }

  @Test
  public void testMetadata() {
    val metadata1 = new Metadata();
    metadata1.setInfo("key1", "f5c9381090a53c54358feb2ba5b7a3d7");

    val metadata1_same = new Metadata();
    metadata1_same.setInfo("key1", "f5c9381090a53c54358feb2ba5b7a3d7");

    assertEntitiesEqual(metadata1, metadata1_same, true);

    val metadata2 = new Metadata();
    metadata2.setInfo("key2", "6329334b-dcd5-53c8-98fd-9812ac386d30");

    assertEntitiesNotEqual(metadata1, metadata2);

    metadata2.addInfo(metadata1.getInfoAsString());
    assertTrue(metadata2.getInfo().has("key1"));
    assertEquals(metadata2.getInfo().path("key1").textValue(), "f5c9381090a53c54358feb2ba5b7a3d7");

    metadata2.setInfo("something that is not json");
    assertTrue(metadata2.getInfo().has("info"));
    assertEquals(metadata2.getInfo().path("info").textValue(), "something that is not json");
  }

  @Test
  public void testUpload() {
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

    val u1_same =
        Upload.builder()
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

    val u2 =
        Upload.builder()
            .uploadId("uploadId2")
            .studyId("study333")
            .analysisId("an2")
            .state(UploadStates.VALIDATION_ERROR.toString())
            .errors("error2")
            .payload("payload2")
            .updatedAt(LocalDateTime.MIN)
            .createdAt(LocalDateTime.MAX)
            .build();
    assertEntitiesNotEqual(u1, u2);

    // Test getters
    assertEquals(u1.getAnalysisId(), "an1");
    assertEquals(u1.getCreatedAt(), LocalDateTime.MAX);
    assertEquals(u1.getErrors(), newArrayList("error1"));
    assertEquals(u1.getPayload(), "payload1");
    assertEquals(u1.getState(), UploadStates.CREATED.getText());
    assertEquals(u1.getStudyId(), DEFAULT_STUDY_ID);
    assertEquals(u1.getUpdatedAt(), LocalDateTime.MIN);
    assertEquals(u1.getUploadId(), "uploadId1");

    u1.setErrors("error1|error2|error3");
    assertThat(u1.getErrors(), containsInAnyOrder("error1", "error2", "error3"));
    assertEquals(u1.getErrors().size(), 3);

    u1.addErrors(newArrayList("error4", "error5"));
    assertThat(
        u1.getErrors(), containsInAnyOrder("error1", "error2", "error3", "error4", "error5"));
    assertEquals(u1.getErrors().size(), 5);
  }

  @Test
  public void testLegacyEntity() {
    val e1 =
        LegacyEntity.builder()
            .access("open")
            .fileName("f1")
            .gnosId("g1")
            .id("i1")
            .projectCode("p1")
            .build();

    val e1_same =
        LegacyEntity.builder()
            .access("open")
            .fileName("f1")
            .gnosId("g1")
            .id("i1")
            .projectCode("p1")
            .build();

    assertEntitiesEqual(e1, e1_same, true);

    val e2 =
        LegacyEntity.builder()
            .access("open")
            .fileName("f2")
            .gnosId("g2")
            .id("i2")
            .projectCode("p2")
            .build();
    assertEntitiesNotEqual(e1, e2);

    // Test getters
    assertEquals(e1.getAccess(), "open");
    assertEquals(e1.getFileName(), "f1");
    assertEquals(e1.getGnosId(), "g1");
    assertEquals(e1.getId(), "i1");
    assertEquals(e1.getProjectCode(), "p1");
  }

  @Test
  public void testDonor() {
    val donor1 = new Donor();
    donor1.setGender("male");
    donor1.setSubmitterDonorId("myDonorSubmitter1");
    donor1.setDonorId("myDonor1");
    donor1.setStudyId("study1");

    val donor1_same =
        Donor.builder()
            .donorId("myDonor1")
            .submitterDonorId("myDonorSubmitter1")
            .studyId("study1")
            .gender("male")
            .build();
    assertEntitiesEqual(donor1, donor1_same, true);

    val donor2 =
        Donor.builder()
            .donorId("myDonor2")
            .submitterDonorId("myDonorSubmitter2")
            .studyId("study2")
            .gender("female")
            .build();
    assertEntitiesNotEqual(donor1, donor2);

    donor1.setInfo("key1", "f5c9381090a53c54358feb2ba5b7a3d7");
    donor1_same.setInfo("key2", "6329334b-dcd5-53c8-98fd-9812ac386d30");
    assertEntitiesNotEqual(donor1, donor1_same);

    // Test getters
    assertEquals(donor1.getStudyId(), "study1");
    assertEquals(donor1.getGender(), "male");
    assertEquals(donor1.getSubmitterDonorId(), "myDonorSubmitter1");
    assertEquals(donor1.getDonorId(), "myDonor1");
    assertInfoKVPair(donor1, "key1", "f5c9381090a53c54358feb2ba5b7a3d7");
  }

  @Test
  public void testStudy() {
    val study1 = new Study();
    study1.setDescription("a");
    study1.setName("b");
    study1.setOrganization("c");
    study1.setStudyId("d");

    val study1_same =
        Study.builder().studyId("d").name("b").organization("c").description("a").build();

    assertEntitiesEqual(study1, study1_same, true);

    val study2 =
        Study.builder().studyId("d1").name("b1").organization("c1").description("a1").build();
    assertEntitiesNotEqual(study1, study2);

    study1.setInfo("key1", "f5c9381090a53c54358feb2ba5b7a3d7");
    study1_same.setInfo("key2", "6329334b-dcd5-53c8-98fd-9812ac386d30");
    assertEntitiesNotEqual(study1, study1_same);

    // Test getters
    assertEquals(study1.getDescription(), "a");
    assertEquals(study1.getName(), "b");
    assertEquals(study1.getOrganization(), "c");
    assertEquals(study1.getStudyId(), "d");
    assertInfoKVPair(study1, "key1", "f5c9381090a53c54358feb2ba5b7a3d7");
  }

  @Test
  public void testSpecimen() {
    val s1 = new Specimen();
    s1.setDonorId("a1");
    s1.setTumourNormalDesignation(TUMOUR_NORMAL_DESIGNATION.get(0));
    s1.setSpecimenType(SPECIMEN_TYPE.get(0));
    s1.setSpecimenId("b1");
    s1.setSubmitterSpecimenId("c1");
    s1.setSpecimenTissueSource(SPECIMEN_TISSUE_SOURCE.get(0));

    val s1_same =
        Specimen.builder()
            .specimenId("b1")
            .submitterSpecimenId("c1")
            .donorId("a1")
            .tumourNormalDesignation(TUMOUR_NORMAL_DESIGNATION.get(0))
            .specimenTissueSource(SPECIMEN_TISSUE_SOURCE.get(0))
            .specimenType(SPECIMEN_TYPE.get(0))
            .build();

    assertEntitiesEqual(s1, s1_same, true);

    val s2 =
        Specimen.builder()
            .specimenId("b2")
            .submitterSpecimenId("c2")
            .donorId("a2")
            .tumourNormalDesignation(TUMOUR_NORMAL_DESIGNATION.get(1))
            .specimenTissueSource(SPECIMEN_TISSUE_SOURCE.get(1))
            .specimenType(SPECIMEN_TYPE.get(1))
            .build();
    assertEntitiesNotEqual(s1, s2);

    s1.setInfo("key1", "f5c9381090a53c54358feb2ba5b7a3d7");
    s1_same.setInfo("key2", "6329334b-dcd5-53c8-98fd-9812ac386d30");
    assertEntitiesNotEqual(s1, s1_same);

    // Test getters
    assertEquals(s1.getTumourNormalDesignation(), TUMOUR_NORMAL_DESIGNATION.get(0));
    assertEquals(s1.getSpecimenType(), SPECIMEN_TYPE.get(0));
    assertEquals(s1.getSpecimenTissueSource(), SPECIMEN_TISSUE_SOURCE.get(0));
    assertEquals(s1.getSubmitterSpecimenId(), "c1");
    assertEquals(s1.getSpecimenId(), "b1");
    assertEquals(s1.getDonorId(), "a1");
    assertInfoKVPair(s1, "key1", "f5c9381090a53c54358feb2ba5b7a3d7");
  }

  @Test
  public void testAnalysisStates() {
    assertEquals(resolveAnalysisState("PUBLISHED"), PUBLISHED);
    assertEquals(resolveAnalysisState("UNPUBLISHED"), UNPUBLISHED);
    assertEquals(resolveAnalysisState("SUPPRESSED"), SUPPRESSED);
    val erroredStates = newArrayList("published", "unpublished", "suppressed", "anything");
    for (val state : erroredStates) {
      assertExceptionThrownBy(IllegalStateException.class, () -> resolveAnalysisState(state));
    }
  }

  private static void assertEntitiesEqual(
      Object actual, Object expected, boolean checkFieldByField) {
    if (checkFieldByField) {
      assertEquals(actual, expected);
    }
    assertEquals(actual, expected);
    assertEquals(actual.hashCode(), expected.hashCode());
  }

  private static void assertEntitiesNotEqual(Object actual, Object expected) {
    assertNotEquals(actual, expected);
    assertNotEquals(actual.hashCode(), expected.hashCode());
  }
}
