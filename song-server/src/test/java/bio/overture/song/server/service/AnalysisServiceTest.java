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

import bio.overture.song.core.model.enums.AnalysisStates;
import bio.overture.song.core.testing.SongErrorAssertions;
import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.model.Metadata;
import bio.overture.song.server.model.analysis.AbstractAnalysis;
import bio.overture.song.server.model.analysis.SequencingReadAnalysis;
import bio.overture.song.server.model.analysis.VariantCallAnalysis;
import bio.overture.song.server.model.entity.FileEntity;
import bio.overture.song.server.model.entity.Sample;
import bio.overture.song.server.model.entity.composites.CompositeEntity;
import bio.overture.song.server.model.enums.AnalysisTypes;
import bio.overture.song.server.repository.*;
import bio.overture.song.server.utils.generator.AnalysisGenerator;
import bio.overture.song.server.utils.generator.PayloadGenerator;
import bio.overture.song.server.utils.generator.StudyGenerator;
import bio.overture.song.server.utils.securestudy.impl.SecureAnalysisTester;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.id.client.core.IdClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static bio.overture.song.core.exceptions.ServerErrors.*;
import static bio.overture.song.core.model.enums.AnalysisStates.*;
import static bio.overture.song.core.testing.SongErrorAssertions.assertSongError;
import static bio.overture.song.core.utils.JsonUtils.fromJson;
import static bio.overture.song.core.utils.JsonUtils.toJson;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.model.enums.AnalysisTypes.SEQUENCING_READ;
import static bio.overture.song.server.model.enums.AnalysisTypes.VARIANT_CALL;
import static bio.overture.song.server.repository.search.IdSearchRequest.createIdSearchRequest;
import static bio.overture.song.server.utils.TestFiles.*;
import static bio.overture.song.server.utils.generator.AnalysisGenerator.createAnalysisGenerator;
import static bio.overture.song.server.utils.generator.PayloadGenerator.createPayloadGenerator;
import static bio.overture.song.server.utils.generator.StudyGenerator.createStudyGenerator;
import static bio.overture.song.server.utils.securestudy.impl.SecureAnalysisTester.createSecureAnalysisTester;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.*;
import static java.util.stream.IntStream.range;
import static org.assertj.core.api.Assertions.*;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class AnalysisServiceTest {

  private static final String ANALYSIS_INFO_SERVICE = "analysisInfoService";
  private static final String DEFAULT_STUDY_ID = "ABC123";
  private static final String DEFAULT_ANALYSIS_ID = "AN1";
  private static final Set<String> PUBLISHED_ONLY = ImmutableSet.of(PUBLISHED.toString());
  private static final Set<String> ALL_STATES = stream(AnalysisStates.values())
      .map(AnalysisStates::toString)
      .collect(toImmutableSet());

  @Autowired
  FileService fileService;
  @Autowired
  AnalysisService service;
  @Autowired
  IdService idService;
  @Autowired
  private StudyService studyService;
  @Autowired
  private SampleRepository sampleRepository;
  @Autowired
  private AnalysisRepository analysisRepository;
  @Autowired
  private SequencingReadRepository sequencingReadRepository;
  @Autowired
  private VariantCallRepository variantCallRepository;
  @Autowired
  private FileRepository fileRepository;
  @Autowired
  private SampleSetRepository sampleSetRepository;
  @Autowired
  private IdClient idClient;

  private final RandomGenerator randomGenerator = createRandomGenerator(AnalysisServiceTest.class.getSimpleName(), 1539118165994L);//createRandomGenerator(AnalysisServiceTest.class.getSimpleName());

  private PayloadGenerator payloadGenerator;
  private AnalysisGenerator analysisGenerator;
  private StudyGenerator studyGenerator;
  private SecureAnalysisTester secureAnalysisTester;

  @Autowired
  private RetryTemplate retryTemplate;

  @Before
  public void beforeTest(){
    assertThat(studyService.isStudyExist(DEFAULT_STUDY_ID)).isTrue();
    assertThat(service.isAnalysisExist(DEFAULT_ANALYSIS_ID)).isTrue();
  }

  /**
   * This is dirty, but since the existenceService is so easy to construct
   * and the storage url port is randomly assigned, it's worth it.
   */
  @Before
  public void init(){
    this.payloadGenerator = createPayloadGenerator(randomGenerator);
    this.analysisGenerator = createAnalysisGenerator(DEFAULT_STUDY_ID, service, randomGenerator);
    this.studyGenerator = createStudyGenerator(studyService, randomGenerator);
    this.secureAnalysisTester = createSecureAnalysisTester(randomGenerator, studyService, service);
  }

  @Test
  @Transactional
  public void testReadState(){
    val a = service.securedDeepRead(DEFAULT_STUDY_ID, DEFAULT_ANALYSIS_ID);
    val expectedState = resolveAnalysisState(a.getAnalysisState());
    val actualState = service.readState(a.getAnalysisId());
    assertEquals(actualState,expectedState);
  }

  @Test
  @Transactional
  public void testCreateAndUpdate() {
    val created = analysisGenerator.createDefaultRandomSequencingReadAnalysis();
    val analysisId = created.getAnalysisId();
    assertEquals(created.getAnalysisId(),analysisId);
    assertEquals(created.getAnalysisState(),"UNPUBLISHED");
    assertEquals(created.getAnalysisType(),"sequencingRead");
    assertEquals(created.getSample().size(),1);
    val sample = created.getSample().get(0);
    val experiment = ((SequencingReadAnalysis) created).getExperiment();
    assertNotNull(experiment);
    assertThat(experiment.getAlignmentTool().equals("BigWrench"));
    val expectedMetadata = new Metadata();
    expectedMetadata.setInfo("marginOfError", "0.01%");
    assertEquals(experiment.getInfo(),expectedMetadata.getInfo());

;    // test update
    val change="ModifiedToolName";
    experiment.setAlignmentTool(change);
    service.updateAnalysis(DEFAULT_STUDY_ID, created);
    val gotBack = service.securedDeepRead(DEFAULT_STUDY_ID, analysisId);
    val experiment2 =((SequencingReadAnalysis)gotBack).getExperiment();
    assertEquals(experiment2.getAlignmentTool() ,change);

    log.info(format("Created '%s'",toJson(created)));
  }

  @Test
  @Transactional
  public void testIsAnalysisExist(){
    val analysis = payloadGenerator.generateDefaultRandomPayload(VariantCallAnalysis.class);
    val randomAnalysisId = randomGenerator.generateRandomUUIDAsString();
    analysis.setAnalysisId(randomAnalysisId);
    assertThat(service.isAnalysisExist(randomAnalysisId)).isFalse();
    val actualAnalysisId = service.create(DEFAULT_STUDY_ID, analysis, false);
    assertEquals(actualAnalysisId,randomAnalysisId);
    assertThat(service.isAnalysisExist(randomAnalysisId)).isTrue();
  }

  @Test
  @Transactional
  public void testCreateAndUpdateVariantCall() {
    val created = analysisGenerator.createRandomAnalysis(VariantCallAnalysis.class,
        "documents/variantcall-valid-1.json");
    val analysisId = created.getAnalysisId();
    assertEquals(created.getAnalysisId(),analysisId);
    assertEquals(created.getAnalysisState(),UNPUBLISHED.toString());
    assertEquals(created.getAnalysisType(),"variantCall");
    assertEquals(created.getSample().size(),1);
    val sample = created.getSample().get(0);
    val experiment = ((VariantCallAnalysis) created).getExperiment();
    assertNotNull(experiment);
    assertEquals(experiment.getVariantCallingTool(),"silver bullet");
    assertThat(experiment.getInfoAsString()).isEqualTo(
            JsonUtils.fromSingleQuoted("{\"extraInfo\":\"this is extra info\"}"));
    // test update
    val change="GoldenHammer";
    experiment.setVariantCallingTool(change) ;
    service.updateAnalysis(DEFAULT_STUDY_ID, created);
    val gotBack = service.securedDeepRead(DEFAULT_STUDY_ID, analysisId);
    val experiment2 =((VariantCallAnalysis)gotBack).getExperiment();
    assertEquals(experiment2.getVariantCallingTool(),change);

    log.info(format("Created '%s'",toJson(created)));
  }

  @Test
  @Transactional
  public void testReadAnalysisDNE() {
    val nonExistentAnalysisId = analysisGenerator.generateNonExistingAnalysisId();
    assertSongError(() -> service.securedDeepRead(DEFAULT_STUDY_ID, nonExistentAnalysisId), ANALYSIS_ID_NOT_FOUND);
    assertSongError(() -> service.unsecuredDeepRead(nonExistentAnalysisId), ANALYSIS_ID_NOT_FOUND);
  }

  @Test
  @Transactional
  public void testReadVariantCallDNE() {

    val analysis = analysisGenerator.createDefaultRandomVariantCallAnalysis();
    val analysisId = analysis.getAnalysisId();

    variantCallRepository.deleteById(analysisId);
    assertTrue(variantCallRepository.findById(analysisId).isEmpty());
    assertSongError(() -> service.securedDeepRead(analysis.getStudy(), analysisId), VARIANT_CALL_NOT_FOUND);
    assertSongError(() -> service.unsecuredDeepRead(analysisId), VARIANT_CALL_NOT_FOUND);
  }

  @Test
  @Transactional
  public void testReadSequencingReadDNE() {

    val analysis = analysisGenerator.createDefaultRandomSequencingReadAnalysis();
    val analysisId = analysis.getAnalysisId();

    sequencingReadRepository.deleteById(analysisId);
    assertTrue(sequencingReadRepository.findById(analysisId).isEmpty());
    assertSongError(() -> service.securedDeepRead(analysis.getStudy(), analysisId), SEQUENCING_READ_NOT_FOUND);
    assertSongError(() -> service.unsecuredDeepRead(analysisId), SEQUENCING_READ_NOT_FOUND);
  }

  @Test
  @Transactional
  public void testReadVariantCall(){
    val json = getJsonStringFromClasspath("documents/variantcall-read-test.json");
    val analysisRaw = fromJson(json, VariantCallAnalysis.class);
    val analysisId = service.create(DEFAULT_STUDY_ID, analysisRaw, false);
    val a = (VariantCallAnalysis)service.securedDeepRead(DEFAULT_STUDY_ID, analysisId);
    val aUnsecured = (VariantCallAnalysis)service.unsecuredDeepRead(analysisId);
    assertEquals(a,aUnsecured);

    //Asserting Analysis
    assertEquals(a.getAnalysisState(),"UNPUBLISHED");
    assertEquals(a.getAnalysisType(),"variantCall");
    assertEquals(a.getStudy(),DEFAULT_STUDY_ID);
    assertInfoKVPair(a, "description1","description1 for this variantCall analysis an01" );
    assertInfoKVPair(a, "description2","description2 for this variantCall analysis an01" );

    val experiment = a.getExperiment();
    assertEquals(experiment.getAnalysisId(),analysisId);
    assertEquals(experiment.getVariantCallingTool(),"silver bullet ex01");
    assertEquals(experiment.getMatchedNormalSampleSubmitterId(),"sample x24-11a");
    assertInfoKVPair(experiment, "extraExperimentInfo","some more data for a variantCall experiment ex01");

    //Asserting Sample
    assertThat(a.getSample()).hasSize(2);
    val sample0 = a.getSample().stream()
            .filter(x -> x.getSampleSubmitterId().equals("internal_sample_98024759826836_fs01"))
            .findAny()
            .orElse(null);
    assertEquals(sample0.getSampleType(),"Total RNA");
    assertInfoKVPair(sample0, "extraSampleInfo","some more data for a variantCall sample_fs01");

    val donor00 = sample0.getDonor();
    assertEquals(donor00.getStudyId(),DEFAULT_STUDY_ID);
    assertEquals(donor00.getDonorGender(),"male");
    assertEquals(donor00.getDonorSubmitterId(),"internal_donor_123456789-00_fs01");
    assertInfoKVPair(donor00, "extraDonorInfo", "some more data for a variantCall donor_fs01");

    val specimen00 = sample0.getSpecimen();
    assertEquals(specimen00.getDonorId(),donor00.getDonorId());
    assertEquals(specimen00.getSpecimenClass(),"Tumour");
    assertEquals(specimen00.getSpecimenType(),"Primary tumour - other");
    assertEquals(sample0.getSpecimenId(),specimen00.getSpecimenId());
    assertInfoKVPair(specimen00, "extraSpecimenInfo_0", "first for a variantCall specimen_fs01");
    assertInfoKVPair(specimen00, "extraSpecimenInfo_1", "second data for a variantCall specimen_fs01");

    val sample1 = a.getSample().stream()
            .filter(x -> x.getSampleSubmitterId().equals("internal_sample_98024759826836_fs02"))
            .findAny()
            .orElse(null);
    assertEquals(sample1.getSampleSubmitterId(),"internal_sample_98024759826836_fs02");
    assertEquals(sample1.getSampleType(),"Total RNA");
    assertInfoKVPair(sample1, "extraSampleInfo","some more data for a variantCall sample_fs02");

    val donor01 = sample1.getDonor();
    assertEquals(donor01.getStudyId(),DEFAULT_STUDY_ID);
    assertEquals(donor01.getDonorGender(),"female");
    assertEquals(donor01.getDonorSubmitterId(),"internal_donor_123456789-00_fs02");
    assertInfoKVPair(donor01, "extraDonorInfo_0", "first data for a variantCall donor_fs02");
    assertInfoKVPair(donor01, "extraDonorInfo_1","second data for a variantCall donor_fs02");

    val specimen01 = sample1.getSpecimen();
    assertEquals(specimen01.getDonorId(),donor01.getDonorId());
    assertEquals(specimen01.getSpecimenClass(),"Tumour");
    assertEquals(specimen01.getSpecimenType(),"Primary tumour - other");
    assertEquals(sample1.getSpecimenId(),specimen01.getSpecimenId());
    assertInfoKVPair(specimen01, "extraSpecimenInfo", "some more data for a variantCall specimen_fs02");

    assertThat(a.getFile()).hasSize(3);
    val file0 = a.getFile().get(0);
    val file1 = a.getFile().get(1);
    val file2 = a.getFile().get(2);
    assertEquals(file0.getAnalysisId(),analysisId);
    assertEquals(file1.getAnalysisId(),analysisId);
    assertEquals(file2.getAnalysisId(),analysisId);
    assertEquals(file0.getStudyId(),DEFAULT_STUDY_ID);
    assertEquals(file1.getStudyId(),DEFAULT_STUDY_ID);
    assertEquals(file2.getStudyId(),DEFAULT_STUDY_ID);

    val fileName0 = "a3bc0998a-3521-43fd-fa10-a834f3874e46-fn1.MUSE_1-0rc-vcf.20170711.somatic.snv_mnv.vcf.gz";
    val fileName1 ="a3bc0998a-3521-43fd-fa10-a834f3874e46-fn2.MUSE_1-0rc-vcf.20170711.somatic.snv_mnv.vcf.gz";
    val fileName2 = "a3bc0998a-3521-43fd-fa10-a834f3874e46-fn3.MUSE_1-0rc-vcf.20170711.somatic.snv_mnv.vcf.gz.idx";

    for (val file : a.getFile()){
      if (file.getFileName().equals(fileName0)){
        assertEquals(file.getFileName(),fileName0);
        assertEquals(file.getFileSize(),376953);
        assertEquals(file.getFileMd5sum(),"652b2e2b7133229a89650de27ad7fc41");
        assertEquals(file.getFileAccess(),"controlled");
        assertEquals(file.getFileType(),"VCF");
        assertInfoKVPair(file, "extraFileInfo_0", "first data for variantCall file_fn1");
        assertInfoKVPair(file, "extraFileInfo_1", "second data for variantCall file_fn1");
      } else if (file.getFileName().equals(fileName1)){
        assertEquals(file.getFileName(),fileName1);
        assertEquals(file.getFileSize(),983820);
        assertEquals(file.getFileMd5sum(),"b8b743a499e461922accad58fdbf25d2");
        assertEquals(file.getFileAccess(),"open");
        assertEquals(file.getFileType(),"VCF");
        assertInfoKVPair(file, "extraFileInfo", "some more data for variantCall file_fn2");

      } else if (file.getFileName().equals(fileName2)){
        assertEquals(file.getFileName(),fileName2);
        assertEquals(file.getFileSize(),4840);
        assertEquals(file.getFileMd5sum(),"2b80298c2f312df7db482105053f889b");
        assertEquals(file.getFileAccess(),"open");
        assertEquals(file.getFileType(),"IDX");
        assertInfoKVPair(file, "extraFileInfo", "some more data for variantCall file_fn3");
      } else {
        fail(String.format("the fileName %s is not recognized", file.getFileName()));
      }
    }
  }

  @Test
  @Transactional
  public void testReadSequencingRead(){
    val json = getJsonStringFromClasspath("documents/sequencingread-read-test.json");
    val analysisRaw = fromJson(json, SequencingReadAnalysis.class);
    val analysisId = service.create(DEFAULT_STUDY_ID, analysisRaw, false);
    val a = (SequencingReadAnalysis)service.securedDeepRead(DEFAULT_STUDY_ID, analysisId);
    val aUnsecured = (SequencingReadAnalysis)service.unsecuredDeepRead(analysisId);
    assertEquals(a,aUnsecured);

    //Asserting Analysis
    assertEquals(a.getAnalysisState(),"UNPUBLISHED");
    assertEquals(a.getAnalysisType(),"sequencingRead");
    assertEquals(a.getStudy(),DEFAULT_STUDY_ID);
    assertInfoKVPair(a, "description1","description1 for this sequencingRead analysis an01" );
    assertInfoKVPair(a, "description2","description2 for this sequencingRead analysis an01" );

    val experiment = a.getExperiment();
    assertEquals(experiment.getAnalysisId(),analysisId);
    assertEquals(experiment.getLibraryStrategy(),"WXS");
    assertThat(experiment.getPairedEnd()).isFalse();
    assertEquals(experiment.getInsertSize(),92736);
    assertThat(experiment.getAligned()).isTrue();
    assertEquals(experiment.getAlignmentTool(),"myCool Sequence ReadingTool");
    assertEquals(experiment.getReferenceGenome(),"someSeq Genome");
    assertInfoKVPair(experiment, "extraExperimentInfo", "some more data for a sequencingRead experiment ex02");

    val sampleMap = Maps.<String, CompositeEntity>newHashMap();

    //Asserting Sample
    assertThat(a.getSample()).hasSize(2);
    val sample0 = a.getSample().stream()
            .filter(x -> x.getSampleSubmitterId().equals("internal_sample_98024759826836_fr01"))
            .findFirst().get();
    sampleMap.put(sample0.getSampleId(), sample0);
    assertEquals(sample0.getSampleType(),"Total RNA");
    assertInfoKVPair(sample0, "extraSampleInfo","some more data for a sequencingRead sample_fr01");

    val donor00 = sample0.getDonor();
    assertEquals(donor00.getStudyId(),DEFAULT_STUDY_ID);
    assertEquals(donor00.getDonorGender(),"male");
    assertEquals(donor00.getDonorSubmitterId(),"internal_donor_123456789-00_fr01");
    assertInfoKVPair(donor00, "extraDonorInfo", "some more data for a sequencingRead donor_fr01");

    val specimen00 = sample0.getSpecimen();
    assertEquals(specimen00.getDonorId(),donor00.getDonorId());
    assertEquals(specimen00.getSpecimenClass(),"Tumour");
    assertEquals(specimen00.getSpecimenType(),"Primary tumour - other");
    assertEquals(sample0.getSpecimenId(),specimen00.getSpecimenId());
    assertInfoKVPair(specimen00, "extraSpecimenInfo_0", "first for a sequencingRead specimen_fr01");
    assertInfoKVPair(specimen00, "extraSpecimenInfo_1", "second data for a sequencingRead specimen_fr01");

    val sample1 = a.getSample().stream()
            .filter(x -> x.getSampleSubmitterId().equals("internal_sample_98024759826836_fr02"))
            .findFirst().get();
    sampleMap.put(sample1.getSampleId(), sample1);
    assertEquals(sample1.getSampleType(),"Total RNA");
    assertInfoKVPair(sample1, "extraSampleInfo","some more data for a sequencingRead sample_fr02");

    val donor01 = sample1.getDonor();
    assertEquals(donor01.getStudyId(),DEFAULT_STUDY_ID);
    assertEquals(donor01.getDonorGender(),"female");
    assertEquals(donor01.getDonorSubmitterId(),"internal_donor_123456789-00_fr02");
    assertInfoKVPair(donor01, "extraDonorInfo_0", "first data for a sequencingRead donor_fr02");
    assertInfoKVPair(donor01, "extraDonorInfo_1","second data for a sequencingRead donor_fr02");

    val specimen01 = sample1.getSpecimen();
    assertEquals(specimen01.getDonorId(),donor01.getDonorId());
    assertEquals(specimen01.getSpecimenClass(),"Tumour");
    assertEquals(specimen01.getSpecimenType(),"Primary tumour - other");
    assertEquals(sample1.getSpecimenId(),specimen01.getSpecimenId());
    assertInfoKVPair(specimen01, "extraSpecimenInfo", "some more data for a sequencingRead specimen_fr02");

    assertThat(a.getFile()).hasSize(3);
    val file0 = a.getFile().get(0);
    val file1 = a.getFile().get(1);
    val file2 = a.getFile().get(2);
    assertEquals(file0.getAnalysisId(),analysisId);
    assertEquals(file1.getAnalysisId(),analysisId);
    assertEquals(file2.getAnalysisId(),analysisId);
    assertEquals(file0.getStudyId(),DEFAULT_STUDY_ID);
    assertEquals(file1.getStudyId(),DEFAULT_STUDY_ID);
    assertEquals(file2.getStudyId(),DEFAULT_STUDY_ID);

    val fileName0 = "a3bc0998a-3521-43fd-fa10-a834f3874e46-fn1.MUSE_1-0rc-vcf.20170711.bam";
    val fileName1 = "a3bc0998a-3521-43fd-fa10-a834f3874e46-fn2.MUSE_1-0rc-vcf.20170711.bam";
    val fileName2 = "a3bc0998a-3521-43fd-fa10-a834f3874e46-fn3.MUSE_1-0rc-vcf.20170711.bam.bai";
    val fileMap = Maps.<String, FileEntity>newHashMap();

    for (val file : a.getFile()){
      fileMap.put(file.getFileName(), file);
      if (file.getFileName().equals(fileName0)){
        assertEquals(file.getFileName(),fileName0);
        assertEquals(file.getFileSize(),1212121);
        assertEquals(file.getFileMd5sum(),"e2324667df8085eddfe95742047e153f");
        assertEquals(file.getFileAccess(),"controlled");
        assertEquals(file.getFileType(),"BAM");
        assertInfoKVPair(file, "extraFileInfo_0", "first data for sequencingRead file_fn1");
        assertInfoKVPair(file, "extraFileInfo_1", "second data for sequencingRead file_fn1");
      } else if (file.getFileName().equals(fileName1)){
        assertEquals(file.getFileName(),fileName1);
        assertEquals(file.getFileSize(),34343);
        assertEquals(file.getFileMd5sum(),"8b5379a29aac642d6fe1808826bd9e49");
        assertEquals(file.getFileAccess(),"open");
        assertEquals(file.getFileType(),"BAM");
        assertInfoKVPair(file, "extraFileInfo", "some more data for sequencingRead file_fn2");

      } else if (file.getFileName().equals(fileName2)){
        assertEquals(file.getFileName(),fileName2);
        assertEquals(file.getFileSize(),4840);
        assertEquals(file.getFileMd5sum(),"61da923f32863a9c5fa3d2a0e19bdee3");
        assertEquals(file.getFileAccess(),"open");
        assertEquals(file.getFileType(),"BAI");
        assertInfoKVPair(file, "extraFileInfo", "some more data for sequencingRead file_fn3");
      } else {
        fail(String.format("the fileName %s is not recognized", file.getFileName()));
      }
    }

    // Test the readFiles method
    for (val file : service.unsecuredReadFiles(analysisId)){
      assertThat(fileMap).containsKeys(file.getFileName());
      assertEquals(file,fileMap.get(file.getFileName()));
    }

    // Test readSample method
    for (val compositeEntity: service.readSamples(analysisId)){
      assertThat(sampleMap).containsKeys(compositeEntity.getSampleId());
      assertEquals(compositeEntity,sampleMap.get(compositeEntity.getSampleId()));
    }

    assertEquals(service.readSequencingRead(analysisId),experiment);
  }

  @Test
  @Transactional
  public void testSuppress() {
    val an = analysisGenerator.createDefaultRandomAnalysis(SequencingReadAnalysis.class);
    assertEquals(an.getAnalysisState(),"UNPUBLISHED");
    val id = an.getAnalysisId();
    val studyId = an.getStudy();

    service.suppress(studyId, id);
    val analysis = service.securedDeepRead(studyId, id);
    assertEquals(analysis.getAnalysisState(),"SUPPRESSED");
  }

  @Test
  @Transactional
  public void testReadFiles() {
    val files = service.unsecuredReadFiles(DEFAULT_ANALYSIS_ID);
    System.err.printf("Got files '%s'", files);
    val expectedFiles = new ArrayList<FileEntity>();

    expectedFiles.add(fileService.securedRead(DEFAULT_STUDY_ID, "FI1"));
    expectedFiles.add(fileService.securedRead(DEFAULT_STUDY_ID, "FI2"));

    assertThat(files).containsAll(expectedFiles);
    assertThat(expectedFiles).containsAll(files);
    val files2 = service.securedReadFiles(DEFAULT_STUDY_ID, DEFAULT_ANALYSIS_ID);
    assertThat(files2).containsOnlyElementsOf(files);
  }

  @Test
  @Transactional
  public void testReadFilesError() {
    val nonExistingAnalysisId = analysisGenerator.generateNonExistingAnalysisId();
    assertSongError(() -> service.unsecuredReadFiles(nonExistingAnalysisId), ANALYSIS_ID_NOT_FOUND);

  }

  @Test
  @Transactional
  public void testDuplicateAnalysisAttemptError() {
    val an1 = service.securedDeepRead(DEFAULT_STUDY_ID,"AN1");
    assertSongError(() -> service.create(an1.getStudy(), an1, true),
        DUPLICATE_ANALYSIS_ATTEMPT);
  }

  @Test
  @Transactional
  public void testCustomAnalysisId(){
    val study= DEFAULT_STUDY_ID;
    val expectedAnalysisId = "AN-1234";
    val expectedObjectIdMap = Maps.newHashMap();
    expectedObjectIdMap.put("a3bc0998a-3521-43fd-fa10-a834f3874e46.MUSE_1-0rc-vcf.20170711.somatic.snv_mnv.vcf.gz", "0794ae66-80df-5b70-bc22-e49309bfba2a");
    expectedObjectIdMap.put("a3bc0998a-3521-43fd-fa10-a834f3874e46.MUSE_1-0rc-vcf.20170711.somatic.snv_mnv.vcf.gz.idx", "a2449e0a-7020-5f2d-8610-9f58aafd467a" );

    val analysisRaw = payloadGenerator.generateRandomPayload(SequencingReadAnalysis.class,"documents/sequencingread-custom-analysis-id"
        + ".json");
    analysisRaw.setAnalysisId(expectedAnalysisId);
    val actualAnalysisId = service.create(study, analysisRaw, false);
    assertEquals(actualAnalysisId,expectedAnalysisId);
    val analysis = service.securedDeepRead(study, actualAnalysisId);
    for (val file : analysis.getFile()){
      val filename = file.getFileName();
      assertThat(expectedObjectIdMap).containsKey(filename);
      val expectedObjectId = expectedObjectIdMap.get(filename);
      val actualObjectId = file.getObjectId();
      val actualFileAnalysisId = file.getAnalysisId();
      assertEquals(actualObjectId,expectedObjectId);
      assertEquals(actualFileAnalysisId,actualAnalysisId);
    }
  }

  @Test
  @Transactional
  public void testCreateAnalysisStudyDNE(){
    val nonExistentStudyId = randomGenerator.generateRandomUUID().toString();
    assertThat(studyService.isStudyExist(nonExistentStudyId)).isFalse();

    val payload = payloadGenerator.generateDefaultRandomPayload(VariantCallAnalysis.class);
    payload.setAnalysisId(null);

    assertNull(payload.getAnalysisId());
    assertSongError(() -> service.create(nonExistentStudyId, payload, false), STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  @Transactional
  public void testGetAnalysisAndIdSearch(){
    val studyId = studyGenerator.createRandomStudy();

    val analysisGenerator = createAnalysisGenerator(studyId, service, randomGenerator);
    val numAnalysis = 10;
    val sraMap = Maps.<String, SequencingReadAnalysis>newHashMap();
    val vcaMap = Maps.<String, VariantCallAnalysis>newHashMap();
    val expectedAnalyses = Sets.<AbstractAnalysis>newHashSet();
    for (int i=1; i<=numAnalysis; i++){
      if (i%2 == 0){
        val sra = analysisGenerator.createDefaultRandomSequencingReadAnalysis();
        assertThat(sraMap.containsKey(sra.getAnalysisId())).isFalse();
        sraMap.put(sra.getAnalysisId(), sra);
        expectedAnalyses.add(sra);
      } else {
        val vca = analysisGenerator.createDefaultRandomVariantCallAnalysis();
        assertThat(sraMap.containsKey(vca.getAnalysisId())).isFalse();
        vcaMap.put(vca.getAnalysisId(), vca);
        expectedAnalyses.add(vca);
      }
    }
    assertThat(expectedAnalyses).hasSize(numAnalysis);
    assertEquals(sraMap.keySet().size() + vcaMap.keySet().size(),numAnalysis);
    val expectedVCAs = newHashSet(vcaMap.values());
    val expectedSRAs = newHashSet(sraMap.values());
    assertThat(expectedSRAs).hasSize(sraMap.keySet().size());
    assertThat(expectedVCAs).hasSize(vcaMap.keySet().size());

    val actualAnalyses = service.getAnalysis(studyId, ALL_STATES);
    val actualSRAs = actualAnalyses.stream()
        .filter(x -> AnalysisTypes.resolveAnalysisType(x.getAnalysisType()) == AnalysisTypes.SEQUENCING_READ)
        .collect(toSet());
    val actualVCAs = actualAnalyses.stream()
        .filter(x -> AnalysisTypes.resolveAnalysisType(x.getAnalysisType()) == VARIANT_CALL)
        .collect(toSet());

    assertThat(actualSRAs).hasSize(sraMap.keySet().size());
    assertThat(actualVCAs).hasSize(vcaMap.keySet().size());
    assertThat(actualSRAs).containsAll(expectedSRAs);
    assertThat(actualVCAs).containsAll(expectedVCAs);

    // Do a study-wide idSearch and verify the response effectively has the same
    // number of results as the getAnalysis method
    val searchedAnalyses = service.idSearch(studyId,
        createIdSearchRequest(null, null, null, null));
    assertThat(searchedAnalyses).hasSameSizeAs(expectedAnalyses);
    assertThat(searchedAnalyses).containsOnlyElementsOf(expectedAnalyses);
  }

  @Test
  @Transactional
  public void testOnlyGetPublishedAnalyses(){
    val studyId = studyGenerator.createRandomStudy();
    val analysisGenerator = createAnalysisGenerator(studyId, service, randomGenerator);
    val numAnalysis = 10;
    val expectedMap = range(0, numAnalysis)
        .boxed()
        .map(x -> {
          AbstractAnalysis a = analysisGenerator.createDefaultRandomSequencingReadAnalysis();
          AnalysisStates randomState = x == 0 ? PUBLISHED : randomGenerator.randomEnum(AnalysisStates.class);
          a.setAnalysisState(randomState.toString());
          service.securedUpdateState(studyId, a.getAnalysisId(), randomState);
          return a;
        })
        .collect(groupingBy(AbstractAnalysis::getAnalysisState));

    val actualMap = service.getAnalysis(studyId, PUBLISHED_ONLY).stream()
        .collect(groupingBy(AbstractAnalysis::getAnalysisState));

    assertThat(actualMap.keySet()).hasSize(1);
    assertThat(expectedMap).containsKey(PUBLISHED.toString());
    assertThat(actualMap).containsKey(PUBLISHED.toString());
    assertThat(actualMap.get(PUBLISHED.toString())).hasSameSizeAs(expectedMap.get(PUBLISHED.toString()));
    assertThat(actualMap.get(PUBLISHED.toString())).hasSameElementsAs(expectedMap.get(PUBLISHED.toString()));
  }

  @Test
  @Transactional
  public void testGetAnalysisEmptyStudy(){
    val studyId = studyGenerator.createRandomStudy();
    assertTrue(service.getAnalysis(studyId, PUBLISHED_ONLY).isEmpty());
  }

  @Test
  @Transactional
  public void testIdSearchEmptyStudy(){
    val studyId = studyGenerator.createRandomStudy();
    val idSearchRequest = createIdSearchRequest(null, null, null, null);
    assertTrue(service.idSearch(studyId, idSearchRequest).isEmpty());
  }

  @Test
  @Transactional
  public void testGetAnalysisDNEStudy() {
    val nonExistentStudyId = studyGenerator.generateNonExistingStudyId();
    assertSongError(() -> service.getAnalysis(nonExistentStudyId, PUBLISHED_ONLY), STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  @Transactional
  public void testIdSearchDNEStudy(){
    val nonExistentStudyId = studyGenerator.generateNonExistingStudyId();
    val idSearchRequest = createIdSearchRequest(null, null, null, null);
    assertSongError(() -> service.idSearch(nonExistentStudyId, idSearchRequest), STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  @Transactional
  public void testAnalysisMissingFilesException(){
    val analysis1 = analysisGenerator.createDefaultRandomSequencingReadAnalysis();
    val analysisId1 = analysis1.getAnalysisId();

    fileRepository.deleteAllByAnalysisId(analysisId1);
    assertTrue(fileRepository.findAllByAnalysisId(analysisId1).isEmpty());
    assertSongError(() -> service.unsecuredReadFiles(analysisId1), ANALYSIS_MISSING_FILES);
    assertSongError(() -> service.securedReadFiles(analysis1.getStudy(), analysisId1), ANALYSIS_MISSING_FILES);

    val analysis2 = analysisGenerator.createDefaultRandomVariantCallAnalysis();
    val analysisId2 = analysis2.getAnalysisId();
    fileRepository.deleteAllByAnalysisId(analysisId2);
    assertTrue(fileRepository.findAllByAnalysisId(analysisId2).isEmpty());
    assertSongError(() -> service.unsecuredReadFiles(analysisId2), ANALYSIS_MISSING_FILES);
    assertSongError(() -> service.securedReadFiles(analysis2.getStudy(), analysisId2), ANALYSIS_MISSING_FILES);
  }

  @Test
  @Transactional
  public void testSequencingReadAnalysisMissingSamplesException() {
    runAnalysisMissingSamplesTest(SequencingReadAnalysis.class);
    assert(true);
  }

  @Test
  @Transactional
  public void testVariantCallAnalysisMissingSamplesException() {
    runAnalysisMissingSamplesTest(VariantCallAnalysis.class);
    assert(true);
  }

  @Test
  @Transactional
  public void testAnalysisIdDneException(){
    val nonExistentAnalysisId = analysisGenerator.generateNonExistingAnalysisId();
    SongErrorAssertions
        .assertSongErrorRunnable(() -> service.checkAnalysisAndStudyRelated(DEFAULT_STUDY_ID, nonExistentAnalysisId),
        ANALYSIS_ID_NOT_FOUND);
    SongErrorAssertions
        .assertSongErrorRunnable(() -> service.checkAnalysisExists(nonExistentAnalysisId), ANALYSIS_ID_NOT_FOUND);
    assertSongError(() -> service.securedDeepRead(DEFAULT_STUDY_ID ,nonExistentAnalysisId), ANALYSIS_ID_NOT_FOUND);
    assertSongError(() -> service.unsecuredDeepRead(nonExistentAnalysisId), ANALYSIS_ID_NOT_FOUND);
    assertSongError(() -> service.unsecuredReadFiles(nonExistentAnalysisId), ANALYSIS_ID_NOT_FOUND);
    assertSongError(() -> service.securedReadFiles(DEFAULT_STUDY_ID, nonExistentAnalysisId), ANALYSIS_ID_NOT_FOUND);
    assertSongError(() -> service.readSamples(nonExistentAnalysisId), ANALYSIS_ID_NOT_FOUND);
    assertSongError(() -> service.readState(nonExistentAnalysisId), ANALYSIS_ID_NOT_FOUND);
  }

  @Test
  @Transactional
  public void testCheckAnalysisAndStudyRelated(){
    val existingAnalysisId = DEFAULT_ANALYSIS_ID;
    val existingStudyId =  DEFAULT_STUDY_ID;
    assertThat(service.isAnalysisExist(existingAnalysisId)).isTrue();
    assertThat(studyService.isStudyExist(existingStudyId)).isTrue();
    service.checkAnalysisAndStudyRelated(existingStudyId, existingAnalysisId);
    assert(true);
  }

  @Test
  @Transactional
  public void testCheckAnalysisUnrelatedToStudy(){
    secureAnalysisTester.runSecureTest((s,a) -> service.checkAnalysisAndStudyRelated(s, a));
    secureAnalysisTester.runSecureTest((s,a) -> service.securedDeepRead(s, a), VARIANT_CALL);
    secureAnalysisTester.runSecureTest((s,a) -> service.securedDeepRead(s, a), AnalysisTypes.SEQUENCING_READ);
    secureAnalysisTester.runSecureTest((s,a) -> service.suppress(s, a));
    secureAnalysisTester.runSecureTest((s,a) -> service.securedReadFiles(s,a));
    secureAnalysisTester.runSecureTest((s,a) -> service.publish(s, a, false));
    secureAnalysisTester.runSecureTest((s,a) -> service.publish(s, a, true));
  }

  @Test
  @Transactional
  public void testAnalysisExistence(){
    val existingAnalysisId  = DEFAULT_ANALYSIS_ID;
    val nonExistentAnalysisId = randomGenerator.generateRandomUUID().toString();
    assertThat(service.isAnalysisExist(nonExistentAnalysisId)).isFalse();
    assertThat(service.isAnalysisExist(existingAnalysisId)).isTrue();
    assertThat(analysisRepository.existsById(existingAnalysisId)).isTrue();
    assertThat(analysisRepository.existsById(nonExistentAnalysisId)).isFalse();
  }

  @Test
  @Transactional
  public void testGetAnalysisForStudyFilteredByStates(){
    val studyId = studyGenerator.createRandomStudy();
    val generator = createAnalysisGenerator(studyId, service, randomGenerator);

    val numCopies = 2;
    val expectedAnalyses = range(0, numCopies).boxed()
        .flatMap(x -> stream(AnalysisStates.values()))
        .map(x -> createSRAnalysisWithState(generator, x))
        .collect(toImmutableSet());

    // 0 - Empty
    assertGetAnalysesForStudy(expectedAnalyses, studyId);

    // 1 - PUBLISHED
    assertGetAnalysesForStudy(expectedAnalyses, studyId, PUBLISHED);

    // 2 - UNPUBLISHED
    assertGetAnalysesForStudy(expectedAnalyses, studyId, UNPUBLISHED);

    // 3 - SUPPRESSED
    assertGetAnalysesForStudy(expectedAnalyses, studyId, SUPPRESSED);

    // 4 - PUBLISHED,UNPUBLISHED
    assertGetAnalysesForStudy(expectedAnalyses, studyId, PUBLISHED, UNPUBLISHED);

    // 5 - PUBLISHED,SUPPRESSED
    assertGetAnalysesForStudy(expectedAnalyses, studyId, PUBLISHED, SUPPRESSED);

    // 6 - UNPUBLISHED,SUPPRESSED
    assertGetAnalysesForStudy(expectedAnalyses, studyId, UNPUBLISHED, SUPPRESSED);

    // 7 - PUBLISHED,UNPUBLISHED,SUPPRESSED
    assertGetAnalysesForStudy(expectedAnalyses, studyId, UNPUBLISHED, SUPPRESSED, PUBLISHED);

    assertSongError(() -> service.getAnalysis(studyId, newHashSet(PUBLISHED.toString(), "SomethingElse")),
        MALFORMED_PARAMETER);

    assertSongError(() -> service.getAnalysis(studyId, newHashSet("SomethingElse")),
        MALFORMED_PARAMETER);
  }

  private static AnalysisTypes selectAnalysisType(int select){
    return select % 2 == 0 ? VARIANT_CALL : SEQUENCING_READ;
  }

  @Test
  @Transactional
  public void testGetAnalysisForStudyView(){
    val numAnalysesPerStudy = 100;
    val numStudies = 3;
    val studyIds = range(0, numStudies).boxed().map(x -> studyGenerator.createRandomStudy()).collect(toImmutableSet());
    val study2AnalysesMap = studyIds.stream()
        .map(x -> createAnalysisGenerator(x, service, randomGenerator))
        .map(x ->
            range(0, numAnalysesPerStudy).boxed()
                .map(a -> (AbstractAnalysis)x.createDefaultRandomAnalysis(selectAnalysisType(a))))
        .flatMap(x -> x)
        .collect(groupingBy(AbstractAnalysis::getStudy));

    val studyId = study2AnalysesMap.keySet().stream().findFirst().get();
    val expectedAnalyses = study2AnalysesMap.get(studyId);

    val expectedAnalysisMap = expectedAnalyses.stream()
        .collect(toMap(x -> ((AbstractAnalysis) x).getAnalysisId(), x -> x));

    val expectedAnalysisIds = expectedAnalysisMap.keySet();

    val analysisStates = ImmutableSet.of(UNPUBLISHED.toString());

    val actualAnalyses1 = service.getAnalysis(studyId, analysisStates);
    val actualAnalyses2 = service.getAnalysisByView(studyId, analysisStates);

    val actualAnalysisIds1 = actualAnalyses1.stream().map(AbstractAnalysis::getAnalysisId).collect(toImmutableSet());
    val actualAnalysisIds2 = actualAnalyses2.stream().map(AbstractAnalysis::getAnalysisId).collect(toImmutableSet());

    assertSetsMatch(actualAnalysisIds1, expectedAnalysisIds);
    assertSetsMatch(actualAnalysisIds2, expectedAnalysisIds);

    actualAnalyses1.forEach(x -> diff(x, expectedAnalysisMap.get(x.getAnalysisId())) );
    actualAnalyses2.forEach(x -> diff(x, expectedAnalysisMap.get(x.getAnalysisId())) );
  }

  /**
   * Tests that if an error occurs during the create method of the AnalysisService, that any entities created
   * in the method are rolled back (using transactions) and that the id is not committed to the id server.
   * This test does not use the @Transactional because we are testing rolling back of a failed analysisService.create
   * call.
   */
  @Test
  public void testRevokeAnalysisId(){

    // Find an analysisId that is unique and doesnt exist
    val id = idService.resolveAnalysisId("", false);
    assertThat(service.isAnalysisExist(id)).isFalse();

    // Generate a payload using the analysisId
    val payload = payloadGenerator.generateDefaultRandomPayload(SequencingReadAnalysis.class);
    payload.setAnalysisId(id);

    /**
     * Mock the analysisInfoService. This service is called during the create method of AnalysisService
     * and then it exceptions out. Since the analysisService has way to many dependencies (not good),
     * mocking it the right way is ridiculous. Instead, we can mock an internal service (i.e analysisInfoService)
     * using a runtime surgical tool such as ReflectionTestUtils, to replace the actual analysisInfoService with the
     * mocked one to forcefully throw an exception to test the revoke feature. This is a dirty hack and is characteristic
     * of poor design at the service layer.
     */
    val analysisInfoServiceMock = mock(AnalysisInfoService.class);
    doThrow(new IllegalStateException("some error happened during the ")).when(analysisInfoServiceMock).create(id, payload.getInfoAsString());
    val originalAnalysisInfoService = ReflectionTestUtils.getField(service, ANALYSIS_INFO_SERVICE);
    ReflectionTestUtils.setField(service, ANALYSIS_INFO_SERVICE, analysisInfoServiceMock);
    assertThat(service.isAnalysisExist(id)).isFalse();


    // Ensure the mock is used and that an error was actually thrown
    val throwable = catchThrowable(() -> service.create(DEFAULT_STUDY_ID, payload, false));
    assertThat(throwable).as("An exception was not thrown").isInstanceOf(IllegalStateException.class);

    // Ensure everything was rolled back properly
    assertThat(service.isAnalysisExist(id)).isFalse();

    // Ensure the id was not committed to the id server
    assertTrue(idClient.getAnalysisId(id).isEmpty());

    // Plug the original analysisInfoService back into service so other tests can function properly. This is a reset.
    ReflectionTestUtils.setField(service, ANALYSIS_INFO_SERVICE, originalAnalysisInfoService);
  }

  @Test
  public void testUnpublishState() {
    Stream.of(VARIANT_CALL, SEQUENCING_READ).forEach(this::runUnpublishStateTest);
  }

  private void runUnpublishStateTest(AnalysisTypes analysisType){
    val a = analysisGenerator.createDefaultRandomAnalysis(analysisType);
    val analysisId = a.getAnalysisId();
    val studyId = a.getStudy();

    // 1: UNPUBLISHED -> UNPUBLISHED
    service.securedUpdateState(studyId, analysisId, UNPUBLISHED);
    val a11 = service.unsecuredDeepRead(analysisId);
    val actualState11 = resolveAnalysisState(a11.getAnalysisState());
    assertEquals(actualState11,UNPUBLISHED);
    service.unpublish(studyId, analysisId);
    val a12 = service.unsecuredDeepRead(analysisId);
    val actualState12 = resolveAnalysisState(a12.getAnalysisState());
    assertEquals(actualState12,UNPUBLISHED);

    // 2: PUBLISHED -> UNPUBLISHED
    service.securedUpdateState(studyId, analysisId, PUBLISHED);
    val a21 = service.unsecuredDeepRead(analysisId);
    val actualState21 = resolveAnalysisState(a21.getAnalysisState());
    assertEquals(actualState21,PUBLISHED);
    service.unpublish(studyId, analysisId);
    val a22 = service.unsecuredDeepRead(analysisId);
    val actualState22 = resolveAnalysisState(a22.getAnalysisState());
    assertEquals(actualState22,UNPUBLISHED);

    // 3: SUPPRESSED -> UNPUBLISHED
    service.securedUpdateState(studyId, analysisId, SUPPRESSED);
    val a31 = service.unsecuredDeepRead(analysisId);
    val actualState31 = resolveAnalysisState(a31.getAnalysisState());
    assertEquals(actualState31,SUPPRESSED);
    assertSongError(() -> service.unpublish(studyId, analysisId), SUPPRESSED_STATE_TRANSITION);
  }

  private void assertGetAnalysesForStudy(Set<AbstractAnalysis> expectedAnalyses, String studyId, AnalysisStates ... states){
    Set<String> stateStrings = stream(states).map(AnalysisStates::toString).collect(toImmutableSet());
    if (states.length == 0){
      stateStrings = PUBLISHED_ONLY;
    }
    val finalStates = stateStrings;

    val results = service.getAnalysis(studyId, states.length == 0 ? newHashSet() : newHashSet(finalStates));
    val actual = results.stream().map(AbstractAnalysis::getAnalysisId).collect(toImmutableSet());
    val expected = expectedAnalyses.stream()
        .filter(x -> finalStates.contains(x.getAnalysisState()))
        .map(AbstractAnalysis::getAnalysisId)
        .collect(toImmutableSet());
    assertSetsMatch(actual, expected);
  }

  private AbstractAnalysis createSRAnalysisWithState(AnalysisGenerator generator, AnalysisStates state){
    val a = generator.createDefaultRandomSequencingReadAnalysis();
    service.securedUpdateState(a.getStudy(),a.getAnalysisId(), state);
    return service.unsecuredDeepRead(a.getAnalysisId());
  }

  private void runAnalysisMissingSamplesTest(Class<? extends AbstractAnalysis> analysisClass) {
    // Create random analysis,
    val analysis = analysisGenerator.createDefaultRandomAnalysis(analysisClass);
    val analysisId = analysis.getAnalysisId();

    sampleSetRepository.deleteAllBySampleSetPK_AnalysisId(analysisId);
    assertThat(sampleSetRepository.findAllBySampleSetPK_AnalysisId(analysisId)).hasSize(0);
    analysis.getSample().stream()
        .map(Sample::getSampleId)
        .forEach(sampleRepository::deleteById);
    assertSongError(() -> service.readSamples(analysisId), ANALYSIS_MISSING_SAMPLES);
  }

  private static <T,R> void assertFunctionEqual(T l, T r, Function<T,R> trFunction){
    assertEquals(trFunction.apply(l),trFunction.apply(r));
  }

  private static void diff(AbstractAnalysis l, AbstractAnalysis r){
    assertFunctionEqual(l, r, AbstractAnalysis::getAnalysisId);
    assertFunctionEqual(l, r, AbstractAnalysis::getAnalysisState);
    assertFunctionEqual(l, r, AbstractAnalysis::getAnalysisType);
    assertFunctionEqual(l, r, AbstractAnalysis::getStudy);
    assertFunctionEqual(l, r, AbstractAnalysis::getInfoAsString);

    val leftFiles = newHashSet(l.getFile());
    val rightFiles = newHashSet(r.getFile());
    assertSetsMatch(leftFiles, rightFiles);

    val leftSamples = newHashSet(l.getSample());
    val rightSamples = newHashSet(r.getSample());
    assertSetsMatch(leftSamples, rightSamples);

    assertEquals(l.getInfo(),r.getInfo());
    if (l instanceof SequencingReadAnalysis && r instanceof SequencingReadAnalysis){
      assertEquals(((SequencingReadAnalysis)l).getExperiment(),((SequencingReadAnalysis)r).getExperiment());
    } else if (l instanceof VariantCallAnalysis && r instanceof VariantCallAnalysis){
      assertEquals(((VariantCallAnalysis)l).getExperiment(),((VariantCallAnalysis)r).getExperiment());
    }

  }

}
