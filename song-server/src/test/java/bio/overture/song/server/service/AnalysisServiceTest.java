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
import bio.overture.song.server.repository.AnalysisRepository;
import bio.overture.song.server.repository.FileRepository;
import bio.overture.song.server.repository.SampleRepository;
import bio.overture.song.server.repository.SampleSetRepository;
import bio.overture.song.server.repository.SequencingReadRepository;
import bio.overture.song.server.repository.VariantCallRepository;
import bio.overture.song.server.utils.AnalysisGenerator;
import bio.overture.song.server.utils.PayloadGenerator;
import bio.overture.song.server.utils.StudyGenerator;
import bio.overture.song.server.utils.securestudy.impl.SecureAnalysisTester;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.range;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_ID_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_MISSING_FILES;
import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_MISSING_SAMPLES;
import static bio.overture.song.core.exceptions.ServerErrors.DUPLICATE_ANALYSIS_ATTEMPT;
import static bio.overture.song.core.exceptions.ServerErrors.MALFORMED_PARAMETER;
import static bio.overture.song.core.exceptions.ServerErrors.SEQUENCING_READ_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerErrors.STUDY_ID_DOES_NOT_EXIST;
import static bio.overture.song.core.exceptions.ServerErrors.SUPPRESSED_STATE_TRANSITION;
import static bio.overture.song.core.exceptions.ServerErrors.VARIANT_CALL_NOT_FOUND;
import static bio.overture.song.core.model.enums.AnalysisStates.PUBLISHED;
import static bio.overture.song.core.model.enums.AnalysisStates.SUPPRESSED;
import static bio.overture.song.core.model.enums.AnalysisStates.UNPUBLISHED;
import static bio.overture.song.core.model.enums.AnalysisStates.resolveAnalysisState;
import static bio.overture.song.core.testing.SongErrorAssertions.assertSongError;
import static bio.overture.song.core.utils.JsonUtils.fromJson;
import static bio.overture.song.core.utils.JsonUtils.toJson;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.model.enums.AnalysisTypes.SEQUENCING_READ;
import static bio.overture.song.server.model.enums.AnalysisTypes.VARIANT_CALL;
import static bio.overture.song.server.repository.search.IdSearchRequest.createIdSearchRequest;
import static bio.overture.song.server.utils.AnalysisGenerator.createAnalysisGenerator;
import static bio.overture.song.server.utils.PayloadGenerator.createPayloadGenerator;
import static bio.overture.song.server.utils.StudyGenerator.createStudyGenerator;
import static bio.overture.song.server.utils.TestFiles.assertInfoKVPair;
import static bio.overture.song.server.utils.TestFiles.assertSetsMatch;
import static bio.overture.song.server.utils.TestFiles.getJsonStringFromClasspath;
import static bio.overture.song.server.utils.securestudy.impl.SecureAnalysisTester.createSecureAnalysisTester;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("dev")
@Transactional
public class AnalysisServiceTest {

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
  public void testReadState(){
    val a = service.securedDeepRead(DEFAULT_STUDY_ID, DEFAULT_ANALYSIS_ID);
    val expectedState = resolveAnalysisState(a.getAnalysisState());
    val actualState = service.readState(a.getAnalysisId());
    assertThat(actualState).isEqualTo(expectedState);
  }

  @Test
  public void testCreateAndUpdate() {
    val created = analysisGenerator.createDefaultRandomSequencingReadAnalysis();
    val analysisId = created.getAnalysisId();
    assertThat(created.getAnalysisId()).isEqualTo(analysisId);
    assertThat(created.getAnalysisState()).isEqualTo("UNPUBLISHED");
    assertThat(created.getAnalysisType()).isEqualTo("sequencingRead");
    assertThat(created.getSample().size()).isEqualTo(1);
    val sample = created.getSample().get(0);
    val experiment = ((SequencingReadAnalysis) created).getExperiment();
    assertThat(experiment).isNotNull();
    assertThat(experiment.getAlignmentTool().equals("BigWrench"));
    val expectedMetadata = new Metadata();
    expectedMetadata.setInfo("marginOfError", "0.01%");
    assertThat(experiment.getInfo()).isEqualTo(expectedMetadata.getInfo());

;    // test update
    val change="ModifiedToolName";
    experiment.setAlignmentTool(change);
    service.updateAnalysis(DEFAULT_STUDY_ID, created);
    val gotBack = service.securedDeepRead(DEFAULT_STUDY_ID, analysisId);
    val experiment2 =((SequencingReadAnalysis)gotBack).getExperiment();
    assertThat(experiment2.getAlignmentTool() ).isEqualTo(change);

    log.info(format("Created '%s'",toJson(created)));
  }

  @Test
  public void testIsAnalysisExist(){
    val analysis = payloadGenerator.generateDefaultRandomPayload(VariantCallAnalysis.class);
    val randomAnalysisId = randomGenerator.generateRandomUUIDAsString();
    analysis.setAnalysisId(randomAnalysisId);
    assertThat(service.isAnalysisExist(randomAnalysisId)).isFalse();
    val actualAnalysisId = service.create(DEFAULT_STUDY_ID, analysis, false);
    assertThat(actualAnalysisId).isEqualTo(randomAnalysisId);
    assertThat(service.isAnalysisExist(randomAnalysisId)).isTrue();
  }

  @Test
  public void testCreateAndUpdateVariantCall() {
    val created = analysisGenerator.createRandomAnalysis(VariantCallAnalysis.class,
        "documents/variantcall-valid-1.json");
    val analysisId = created.getAnalysisId();
    assertThat(created.getAnalysisId()).isEqualTo(analysisId);
    assertThat(created.getAnalysisState()).isEqualTo(UNPUBLISHED.toString());
    assertThat(created.getAnalysisType()).isEqualTo("variantCall");
    assertThat(created.getSample().size()).isEqualTo(1);
    val sample = created.getSample().get(0);
    val experiment = ((VariantCallAnalysis) created).getExperiment();
    assertThat(experiment).isNotNull();
    assertThat(experiment.getVariantCallingTool()).isEqualTo("silver bullet");
    assertThat(experiment.getInfoAsString()).isEqualTo(
            JsonUtils.fromSingleQuoted("{\"extraInfo\":\"this is extra info\"}"));
    // test update
    val change="GoldenHammer";
    experiment.setVariantCallingTool(change) ;
    service.updateAnalysis(DEFAULT_STUDY_ID, created);
    val gotBack = service.securedDeepRead(DEFAULT_STUDY_ID, analysisId);
    val experiment2 =((VariantCallAnalysis)gotBack).getExperiment();
    assertThat(experiment2.getVariantCallingTool()).isEqualTo(change);

    log.info(format("Created '%s'",toJson(created)));
  }

  @Test
  public void testReadAnalysisDNE() {
    val nonExistentAnalysisId = analysisGenerator.generateNonExistingAnalysisId();
    assertSongError(() -> service.securedDeepRead(DEFAULT_STUDY_ID, nonExistentAnalysisId), ANALYSIS_ID_NOT_FOUND);
    assertSongError(() -> service.unsecuredDeepRead(nonExistentAnalysisId), ANALYSIS_ID_NOT_FOUND);
  }

  @Test
  public void testReadVariantCallDNE() {

    val analysis = analysisGenerator.createDefaultRandomVariantCallAnalysis();
    val analysisId = analysis.getAnalysisId();

    variantCallRepository.deleteById(analysisId);
    assertThat(variantCallRepository.findById(analysisId)).isEmpty();
    assertSongError(() -> service.securedDeepRead(analysis.getStudy(), analysisId), VARIANT_CALL_NOT_FOUND);
    assertSongError(() -> service.unsecuredDeepRead(analysisId), VARIANT_CALL_NOT_FOUND);
  }

  @Test
  public void testReadSequencingReadDNE() {

    val analysis = analysisGenerator.createDefaultRandomSequencingReadAnalysis();
    val analysisId = analysis.getAnalysisId();

    sequencingReadRepository.deleteById(analysisId);
    assertThat(sequencingReadRepository.findById(analysisId)).isEmpty();
    assertSongError(() -> service.securedDeepRead(analysis.getStudy(), analysisId), SEQUENCING_READ_NOT_FOUND);
    assertSongError(() -> service.unsecuredDeepRead(analysisId), SEQUENCING_READ_NOT_FOUND);
  }

  @Test
  public void testReadVariantCall(){
    val json = getJsonStringFromClasspath("documents/variantcall-read-test.json");
    val analysisRaw = fromJson(json, VariantCallAnalysis.class);
    val analysisId = service.create(DEFAULT_STUDY_ID, analysisRaw, false);
    val a = (VariantCallAnalysis)service.securedDeepRead(DEFAULT_STUDY_ID, analysisId);
    val aUnsecured = (VariantCallAnalysis)service.unsecuredDeepRead(analysisId);
    assertThat(a).isEqualTo(aUnsecured);

    //Asserting Analysis
    assertThat(a.getAnalysisState()).isEqualTo("UNPUBLISHED");
    assertThat(a.getAnalysisType()).isEqualTo("variantCall");
    assertThat(a.getStudy()).isEqualTo(DEFAULT_STUDY_ID);
    assertInfoKVPair(a, "description1","description1 for this variantCall analysis an01" );
    assertInfoKVPair(a, "description2","description2 for this variantCall analysis an01" );

    val experiment = a.getExperiment();
    assertThat(experiment.getAnalysisId()).isEqualTo(analysisId);
    assertThat(experiment.getVariantCallingTool()).isEqualTo("silver bullet ex01");
    assertThat(experiment.getMatchedNormalSampleSubmitterId()).isEqualTo("sample x24-11a");
    assertInfoKVPair(experiment, "extraExperimentInfo","some more data for a variantCall experiment ex01");

    //Asserting Sample
    assertThat(a.getSample()).hasSize(2);
    val sample0 = a.getSample().get(0);
    assertThat(sample0.getSampleSubmitterId()).isEqualTo("internal_sample_98024759826836_fs01");
    assertThat(sample0.getSampleType()).isEqualTo("Total RNA");
    assertInfoKVPair(sample0, "extraSampleInfo","some more data for a variantCall sample_fs01");

    val donor00 = sample0.getDonor();
    assertThat(donor00.getStudyId()).isEqualTo(DEFAULT_STUDY_ID);
    assertThat(donor00.getDonorGender()).isEqualTo("male");
    assertThat(donor00.getDonorSubmitterId()).isEqualTo("internal_donor_123456789-00_fs01");
    assertInfoKVPair(donor00, "extraDonorInfo", "some more data for a variantCall donor_fs01");

    val specimen00 = sample0.getSpecimen();
    assertThat(specimen00.getDonorId()).isEqualTo(donor00.getDonorId());
    assertThat(specimen00.getSpecimenClass()).isEqualTo("Tumour");
    assertThat(specimen00.getSpecimenType()).isEqualTo("Primary tumour - other");
    assertThat(sample0.getSpecimenId()).isEqualTo(specimen00.getSpecimenId());
    assertInfoKVPair(specimen00, "extraSpecimenInfo_0", "first for a variantCall specimen_fs01");
    assertInfoKVPair(specimen00, "extraSpecimenInfo_1", "second data for a variantCall specimen_fs01");

    val sample1 = a.getSample().get(1);
    assertThat(sample1.getSampleSubmitterId()).isEqualTo("internal_sample_98024759826836_fs02");
    assertThat(sample1.getSampleType()).isEqualTo("Total RNA");
    assertInfoKVPair(sample1, "extraSampleInfo","some more data for a variantCall sample_fs02");

    val donor01 = sample1.getDonor();
    assertThat(donor01.getStudyId()).isEqualTo(DEFAULT_STUDY_ID);
    assertThat(donor01.getDonorGender()).isEqualTo("female");
    assertThat(donor01.getDonorSubmitterId()).isEqualTo("internal_donor_123456789-00_fs02");
    assertInfoKVPair(donor01, "extraDonorInfo_0", "first data for a variantCall donor_fs02");
    assertInfoKVPair(donor01, "extraDonorInfo_1","second data for a variantCall donor_fs02");

    val specimen01 = sample1.getSpecimen();
    assertThat(specimen01.getDonorId()).isEqualTo(donor01.getDonorId());
    assertThat(specimen01.getSpecimenClass()).isEqualTo("Tumour");
    assertThat(specimen01.getSpecimenType()).isEqualTo("Primary tumour - other");
    assertThat(sample1.getSpecimenId()).isEqualTo(specimen01.getSpecimenId());
    assertInfoKVPair(specimen01, "extraSpecimenInfo", "some more data for a variantCall specimen_fs02");

    assertThat(a.getFile()).hasSize(3);
    val file0 = a.getFile().get(0);
    val file1 = a.getFile().get(1);
    val file2 = a.getFile().get(2);
    assertThat(file0.getAnalysisId()).isEqualTo(analysisId);
    assertThat(file1.getAnalysisId()).isEqualTo(analysisId);
    assertThat(file2.getAnalysisId()).isEqualTo(analysisId);
    assertThat(file0.getStudyId()).isEqualTo(DEFAULT_STUDY_ID);
    assertThat(file1.getStudyId()).isEqualTo(DEFAULT_STUDY_ID);
    assertThat(file2.getStudyId()).isEqualTo(DEFAULT_STUDY_ID);

    val fileName0 = "a3bc0998a-3521-43fd-fa10-a834f3874e46-fn1.MUSE_1-0rc-vcf.20170711.somatic.snv_mnv.vcf.gz";
    val fileName1 ="a3bc0998a-3521-43fd-fa10-a834f3874e46-fn2.MUSE_1-0rc-vcf.20170711.somatic.snv_mnv.vcf.gz";
    val fileName2 = "a3bc0998a-3521-43fd-fa10-a834f3874e46-fn3.MUSE_1-0rc-vcf.20170711.somatic.snv_mnv.vcf.gz.idx";

    for (val file : a.getFile()){
      if (file.getFileName().equals(fileName0)){
        assertThat(file.getFileName()).isEqualTo(fileName0);
        assertThat(file.getFileSize()).isEqualTo(376953);
        assertThat(file.getFileMd5sum()).isEqualTo("652b2e2b7133229a89650de27ad7fc41");
        assertThat(file.getFileAccess()).isEqualTo("controlled");
        assertThat(file.getFileType()).isEqualTo("VCF");
        assertInfoKVPair(file, "extraFileInfo_0", "first data for variantCall file_fn1");
        assertInfoKVPair(file, "extraFileInfo_1", "second data for variantCall file_fn1");
      } else if (file.getFileName().equals(fileName1)){
        assertThat(file.getFileName()).isEqualTo(fileName1);
        assertThat(file.getFileSize()).isEqualTo(983820);
        assertThat(file.getFileMd5sum()).isEqualTo("b8b743a499e461922accad58fdbf25d2");
        assertThat(file.getFileAccess()).isEqualTo("open");
        assertThat(file.getFileType()).isEqualTo("VCF");
        assertInfoKVPair(file, "extraFileInfo", "some more data for variantCall file_fn2");

      } else if (file.getFileName().equals(fileName2)){
        assertThat(file.getFileName()).isEqualTo(fileName2);
        assertThat(file.getFileSize()).isEqualTo(4840);
        assertThat(file.getFileMd5sum()).isEqualTo("2b80298c2f312df7db482105053f889b");
        assertThat(file.getFileAccess()).isEqualTo("open");
        assertThat(file.getFileType()).isEqualTo("IDX");
        assertInfoKVPair(file, "extraFileInfo", "some more data for variantCall file_fn3");
      } else {
        fail(String.format("the fileName %s is not recognized", file.getFileName()));
      }
    }
  }

  @Test
  public void testReadSequencingRead(){
    val json = getJsonStringFromClasspath("documents/sequencingread-read-test.json");
    val analysisRaw = fromJson(json, SequencingReadAnalysis.class);
    val analysisId = service.create(DEFAULT_STUDY_ID, analysisRaw, false);
    val a = (SequencingReadAnalysis)service.securedDeepRead(DEFAULT_STUDY_ID, analysisId);
    val aUnsecured = (SequencingReadAnalysis)service.unsecuredDeepRead(analysisId);
    assertThat(a).isEqualTo(aUnsecured);

    //Asserting Analysis
    assertThat(a.getAnalysisState()).isEqualTo("UNPUBLISHED");
    assertThat(a.getAnalysisType()).isEqualTo("sequencingRead");
    assertThat(a.getStudy()).isEqualTo(DEFAULT_STUDY_ID);
    assertInfoKVPair(a, "description1","description1 for this sequencingRead analysis an01" );
    assertInfoKVPair(a, "description2","description2 for this sequencingRead analysis an01" );

    val experiment = a.getExperiment();
    assertThat(experiment.getAnalysisId()).isEqualTo(analysisId);
    assertThat(experiment.getLibraryStrategy()).isEqualTo("WXS");
    assertThat(experiment.getPairedEnd()).isFalse();
    assertThat(experiment.getInsertSize()).isEqualTo(92736);
    assertThat(experiment.getAligned()).isTrue();
    assertThat(experiment.getAlignmentTool()).isEqualTo("myCool Sequence ReadingTool");
    assertThat(experiment.getReferenceGenome()).isEqualTo("someSeq Genome");
    assertInfoKVPair(experiment, "extraExperimentInfo", "some more data for a sequencingRead experiment ex02");

    val sampleMap = Maps.<String, CompositeEntity>newHashMap();


    //Asserting Sample
    assertThat(a.getSample()).hasSize(2);
    val sample0 = a.getSample().get(0);
    sampleMap.put(sample0.getSampleId(), sample0);
    assertThat(sample0.getSampleSubmitterId()).isEqualTo("internal_sample_98024759826836_fr01");
    assertThat(sample0.getSampleType()).isEqualTo("Total RNA");
    assertInfoKVPair(sample0, "extraSampleInfo","some more data for a sequencingRead sample_fr01");

    val donor00 = sample0.getDonor();
    assertThat(donor00.getStudyId()).isEqualTo(DEFAULT_STUDY_ID);
    assertThat(donor00.getDonorGender()).isEqualTo("male");
    assertThat(donor00.getDonorSubmitterId()).isEqualTo("internal_donor_123456789-00_fr01");
    assertInfoKVPair(donor00, "extraDonorInfo", "some more data for a sequencingRead donor_fr01");

    val specimen00 = sample0.getSpecimen();
    assertThat(specimen00.getDonorId()).isEqualTo(donor00.getDonorId());
    assertThat(specimen00.getSpecimenClass()).isEqualTo("Tumour");
    assertThat(specimen00.getSpecimenType()).isEqualTo("Primary tumour - other");
    assertThat(sample0.getSpecimenId()).isEqualTo(specimen00.getSpecimenId());
    assertInfoKVPair(specimen00, "extraSpecimenInfo_0", "first for a sequencingRead specimen_fr01");
    assertInfoKVPair(specimen00, "extraSpecimenInfo_1", "second data for a sequencingRead specimen_fr01");

    val sample1 = a.getSample().get(1);
    sampleMap.put(sample1.getSampleId(), sample1);
    assertThat(sample1.getSampleSubmitterId()).isEqualTo("internal_sample_98024759826836_fr02");
    assertThat(sample1.getSampleType()).isEqualTo("Total RNA");
    assertInfoKVPair(sample1, "extraSampleInfo","some more data for a sequencingRead sample_fr02");

    val donor01 = sample1.getDonor();
    assertThat(donor01.getStudyId()).isEqualTo(DEFAULT_STUDY_ID);
    assertThat(donor01.getDonorGender()).isEqualTo("female");
    assertThat(donor01.getDonorSubmitterId()).isEqualTo("internal_donor_123456789-00_fr02");
    assertInfoKVPair(donor01, "extraDonorInfo_0", "first data for a sequencingRead donor_fr02");
    assertInfoKVPair(donor01, "extraDonorInfo_1","second data for a sequencingRead donor_fr02");

    val specimen01 = sample1.getSpecimen();
    assertThat(specimen01.getDonorId()).isEqualTo(donor01.getDonorId());
    assertThat(specimen01.getSpecimenClass()).isEqualTo("Tumour");
    assertThat(specimen01.getSpecimenType()).isEqualTo("Primary tumour - other");
    assertThat(sample1.getSpecimenId()).isEqualTo(specimen01.getSpecimenId());
    assertInfoKVPair(specimen01, "extraSpecimenInfo", "some more data for a sequencingRead specimen_fr02");

    assertThat(a.getFile()).hasSize(3);
    val file0 = a.getFile().get(0);
    val file1 = a.getFile().get(1);
    val file2 = a.getFile().get(2);
    assertThat(file0.getAnalysisId()).isEqualTo(analysisId);
    assertThat(file1.getAnalysisId()).isEqualTo(analysisId);
    assertThat(file2.getAnalysisId()).isEqualTo(analysisId);
    assertThat(file0.getStudyId()).isEqualTo(DEFAULT_STUDY_ID);
    assertThat(file1.getStudyId()).isEqualTo(DEFAULT_STUDY_ID);
    assertThat(file2.getStudyId()).isEqualTo(DEFAULT_STUDY_ID);

    val fileName0 = "a3bc0998a-3521-43fd-fa10-a834f3874e46-fn1.MUSE_1-0rc-vcf.20170711.bam";
    val fileName1 = "a3bc0998a-3521-43fd-fa10-a834f3874e46-fn2.MUSE_1-0rc-vcf.20170711.bam";
    val fileName2 = "a3bc0998a-3521-43fd-fa10-a834f3874e46-fn3.MUSE_1-0rc-vcf.20170711.bam.bai";
    val fileMap = Maps.<String, FileEntity>newHashMap();

    for (val file : a.getFile()){
      fileMap.put(file.getFileName(), file);
      if (file.getFileName().equals(fileName0)){
        assertThat(file.getFileName()).isEqualTo(fileName0);
        assertThat(file.getFileSize()).isEqualTo(1212121);
        assertThat(file.getFileMd5sum()).isEqualTo("e2324667df8085eddfe95742047e153f");
        assertThat(file.getFileAccess()).isEqualTo("controlled");
        assertThat(file.getFileType()).isEqualTo("BAM");
        assertInfoKVPair(file, "extraFileInfo_0", "first data for sequencingRead file_fn1");
        assertInfoKVPair(file, "extraFileInfo_1", "second data for sequencingRead file_fn1");
      } else if (file.getFileName().equals(fileName1)){
        assertThat(file.getFileName()).isEqualTo(fileName1);
        assertThat(file.getFileSize()).isEqualTo(34343);
        assertThat(file.getFileMd5sum()).isEqualTo("8b5379a29aac642d6fe1808826bd9e49");
        assertThat(file.getFileAccess()).isEqualTo("open");
        assertThat(file.getFileType()).isEqualTo("BAM");
        assertInfoKVPair(file, "extraFileInfo", "some more data for sequencingRead file_fn2");

      } else if (file.getFileName().equals(fileName2)){
        assertThat(file.getFileName()).isEqualTo(fileName2);
        assertThat(file.getFileSize()).isEqualTo(4840);
        assertThat(file.getFileMd5sum()).isEqualTo("61da923f32863a9c5fa3d2a0e19bdee3");
        assertThat(file.getFileAccess()).isEqualTo("open");
        assertThat(file.getFileType()).isEqualTo("BAI");
        assertInfoKVPair(file, "extraFileInfo", "some more data for sequencingRead file_fn3");
      } else {
        fail(String.format("the fileName %s is not recognized", file.getFileName()));
      }
    }

    // Test the readFiles method
    for (val file : service.unsecuredReadFiles(analysisId)){
      assertThat(fileMap).containsKeys(file.getFileName());
      assertThat(file).isEqualTo(fileMap.get(file.getFileName()));
    }

    // Test readSample method
    for (val compositeEntity: service.readSamples(analysisId)){
      assertThat(sampleMap).containsKeys(compositeEntity.getSampleId());
      assertThat(compositeEntity).isEqualTo(sampleMap.get(compositeEntity.getSampleId()));
    }

    assertThat(service.readSequencingRead(analysisId)).isEqualTo(experiment);
  }

  @Test
  public void testSuppressAndUnpublish() {
    val an = analysisGenerator.createDefaultRandomAnalysis(SequencingReadAnalysis.class);
    assertThat(an.getAnalysisState()).isEqualTo("UNPUBLISHED");
    val id = an.getAnalysisId();
    val studyId = an.getStudy();

    service.suppress(studyId, id);
    val analysis = service.securedDeepRead(studyId, id);
    assertThat(analysis.getAnalysisState()).isEqualTo("SUPPRESSED");

    service.unpublish(studyId, id);
    val analysis2 = service.securedDeepRead(studyId, id);
    assertThat(analysis2.getAnalysisState()).isEqualTo("UNPUBLISHED");
  }

  @Test
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
  public void testReadFilesError() {
    val nonExistingAnalysisId = analysisGenerator.generateNonExistingAnalysisId();
    assertSongError(() -> service.unsecuredReadFiles(nonExistingAnalysisId), ANALYSIS_ID_NOT_FOUND);

  }

  @Test
  public void testDuplicateAnalysisAttemptError() {
    val an1 = service.securedDeepRead(DEFAULT_STUDY_ID,"AN1");
    assertSongError(() -> service.create(an1.getStudy(), an1, true),
        DUPLICATE_ANALYSIS_ATTEMPT);
  }

  @Test
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
    assertThat(actualAnalysisId).isEqualTo(expectedAnalysisId);
    val analysis = service.securedDeepRead(study, actualAnalysisId);
    for (val file : analysis.getFile()){
      val filename = file.getFileName();
      assertThat(expectedObjectIdMap).containsKey(filename);
      val expectedObjectId = expectedObjectIdMap.get(filename);
      val actualObjectId = file.getObjectId();
      val actualFileAnalysisId = file.getAnalysisId();
      assertThat(actualObjectId).isEqualTo(expectedObjectId);
      assertThat(actualFileAnalysisId).isEqualTo(actualAnalysisId);
    }
  }

  @Test
  public void testCreateAnalysisStudyDNE(){
    val nonExistentStudyId = randomGenerator.generateRandomUUID().toString();
    assertThat(studyService.isStudyExist(nonExistentStudyId)).isFalse();

    val payload = payloadGenerator.generateDefaultRandomPayload(VariantCallAnalysis.class);
    payload.setAnalysisId(null);

    assertThat(payload.getAnalysisId()).isNull();
    assertSongError(() -> service.create(nonExistentStudyId, payload, false), STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
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
    assertThat(sraMap.keySet().size() + vcaMap.keySet().size()).isEqualTo(numAnalysis);
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
    assertThat(actualMap.get(PUBLISHED.toString())).containsExactlyElementsOf(expectedMap.get(PUBLISHED.toString()));
  }

  @Test
  public void testGetAnalysisEmptyStudy(){
    val studyId = studyGenerator.createRandomStudy();
    assertThat(service.getAnalysis(studyId, PUBLISHED_ONLY)).isEmpty();
  }

  @Test
  public void testIdSearchEmptyStudy(){
    val studyId = studyGenerator.createRandomStudy();
    val idSearchRequest = createIdSearchRequest(null, null, null, null);
    assertThat(service.idSearch(studyId, idSearchRequest)).isEmpty();
  }

  @Test
  public void testGetAnalysisDNEStudy() {
    val nonExistentStudyId = studyGenerator.generateNonExistingStudyId();
    assertSongError(() -> service.getAnalysis(nonExistentStudyId, PUBLISHED_ONLY), STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  public void testIdSearchDNEStudy(){
    val nonExistentStudyId = studyGenerator.generateNonExistingStudyId();
    val idSearchRequest = createIdSearchRequest(null, null, null, null);
    assertSongError(() -> service.idSearch(nonExistentStudyId, idSearchRequest), STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  public void testAnalysisMissingFilesException(){
    val analysis1 = analysisGenerator.createDefaultRandomSequencingReadAnalysis();
    val analysisId1 = analysis1.getAnalysisId();

    fileRepository.deleteAllByAnalysisId(analysisId1);
    assertThat(fileRepository.findAllByAnalysisId(analysisId1)).isEmpty();
    assertSongError(() -> service.unsecuredReadFiles(analysisId1), ANALYSIS_MISSING_FILES);
    assertSongError(() -> service.securedReadFiles(analysis1.getStudy(), analysisId1), ANALYSIS_MISSING_FILES);

    val analysis2 = analysisGenerator.createDefaultRandomVariantCallAnalysis();
    val analysisId2 = analysis2.getAnalysisId();
    fileRepository.deleteAllByAnalysisId(analysisId2);
    assertThat(fileRepository.findAllByAnalysisId(analysisId2)).isEmpty();
    assertSongError(() -> service.unsecuredReadFiles(analysisId2), ANALYSIS_MISSING_FILES);
    assertSongError(() -> service.securedReadFiles(analysis2.getStudy(), analysisId2), ANALYSIS_MISSING_FILES);
  }

  @Test
  public void testSequencingReadAnalysisMissingSamplesException() {
    runAnalysisMissingSamplesTest(SequencingReadAnalysis.class);
    assert(true);
  }

  @Test
  public void testVariantCallAnalysisMissingSamplesException() {
    runAnalysisMissingSamplesTest(VariantCallAnalysis.class);
    assert(true);
  }

  @Test
  public void testAnalysisIdDneException(){
    val nonExistentAnalysisId = analysisGenerator.generateNonExistingAnalysisId();
    assertSongError(() -> service.checkAnalysisAndStudyRelated(DEFAULT_STUDY_ID, nonExistentAnalysisId),
        ANALYSIS_ID_NOT_FOUND);
    assertSongError(() -> service.checkAnalysisExists(nonExistentAnalysisId), ANALYSIS_ID_NOT_FOUND);
    assertSongError(() -> service.securedDeepRead(DEFAULT_STUDY_ID ,nonExistentAnalysisId), ANALYSIS_ID_NOT_FOUND);
    assertSongError(() -> service.unsecuredDeepRead(nonExistentAnalysisId), ANALYSIS_ID_NOT_FOUND);
    assertSongError(() -> service.unsecuredReadFiles(nonExistentAnalysisId), ANALYSIS_ID_NOT_FOUND);
    assertSongError(() -> service.securedReadFiles(DEFAULT_STUDY_ID, nonExistentAnalysisId), ANALYSIS_ID_NOT_FOUND);
    assertSongError(() -> service.readSamples(nonExistentAnalysisId), ANALYSIS_ID_NOT_FOUND);
    assertSongError(() -> service.readState(nonExistentAnalysisId), ANALYSIS_ID_NOT_FOUND);
  }

  @Test
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
    secureAnalysisTester.runSecureTest((s,a) -> service.publish("mockToken", s, a, false));
    secureAnalysisTester.runSecureTest((s,a) -> service.publish("mockToken", s, a, true));
  }

  @Test
  public void testAnalysisExistence(){
    val existingAnalysisId  = DEFAULT_ANALYSIS_ID;
    val nonExistentAnalysisId = randomGenerator.generateRandomUUID().toString();
    assertThat(service.isAnalysisExist(nonExistentAnalysisId)).isFalse();
    assertThat(service.isAnalysisExist(existingAnalysisId)).isTrue();
    assertThat(analysisRepository.existsById(existingAnalysisId)).isTrue();
    assertThat(analysisRepository.existsById(nonExistentAnalysisId)).isFalse();
  }

  @Test
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
    assertThat(actualState11).isEqualTo(UNPUBLISHED);
    service.unpublish(studyId, analysisId);
    val a12 = service.unsecuredDeepRead(analysisId);
    val actualState12 = resolveAnalysisState(a12.getAnalysisState());
    assertThat(actualState12).isEqualTo(UNPUBLISHED);

    // 2: PUBLISHED -> UNPUBLISHED
    service.securedUpdateState(studyId, analysisId, PUBLISHED);
    val a21 = service.unsecuredDeepRead(analysisId);
    val actualState21 = resolveAnalysisState(a21.getAnalysisState());
    assertThat(actualState21).isEqualTo(PUBLISHED);
    service.unpublish(studyId, analysisId);
    val a22 = service.unsecuredDeepRead(analysisId);
    val actualState22 = resolveAnalysisState(a22.getAnalysisState());
    assertThat(actualState22).isEqualTo(UNPUBLISHED);

    // 3: SUPPRESSED -> UNPUBLISHED
    service.securedUpdateState(studyId, analysisId, SUPPRESSED);
    val a31 = service.unsecuredDeepRead(analysisId);
    val actualState31 = resolveAnalysisState(a31.getAnalysisState());
    assertThat(actualState31).isEqualTo(SUPPRESSED);
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
    analysis.getSample().stream()
        .map(Sample::getSampleId)
        .forEach(sampleRepository::deleteById);
    assertSongError(() -> service.readSamples(analysisId), ANALYSIS_MISSING_SAMPLES);
  }

  private static <T,R> void assertFunctionEqual(T l, T r, Function<T,R> trFunction){
    assertThat(trFunction.apply(l)).isEqualTo(trFunction.apply(r));
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

    assertThat(l.getInfo()).isEqualTo(r.getInfo());
    if (l instanceof SequencingReadAnalysis && r instanceof SequencingReadAnalysis){
      assertThat(((SequencingReadAnalysis)l).getExperiment()).isEqualTo(((SequencingReadAnalysis)r).getExperiment());
    } else if (l instanceof VariantCallAnalysis && r instanceof VariantCallAnalysis){
      assertThat(((VariantCallAnalysis)l).getExperiment()).isEqualTo(((VariantCallAnalysis)r).getExperiment());
    }

  }

}
