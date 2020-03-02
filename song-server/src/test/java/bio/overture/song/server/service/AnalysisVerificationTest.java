package bio.overture.song.server.service;

import bio.overture.song.core.exceptions.ServerException;
import bio.overture.song.core.utils.ResourceFetcher;
import bio.overture.song.server.kafka.Sender;
import bio.overture.song.server.model.analysis.Analysis;
import bio.overture.song.server.model.analysis.AnalysisData;
import bio.overture.song.server.model.dto.VerifierReply;
import bio.overture.song.server.model.entity.AnalysisSchema;
import bio.overture.song.server.model.enums.VerifierStatus;
import bio.overture.song.server.repository.AnalysisDataRepository;
import bio.overture.song.server.repository.AnalysisRepository;
import bio.overture.song.server.repository.FileRepository;
import bio.overture.song.server.repository.SampleSetRepository;
import bio.overture.song.server.repository.search.SearchRepository;
import bio.overture.song.server.service.id.IdService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.jpa.domain.Specification;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static bio.overture.song.core.utils.JsonUtils.fromJson;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AnalysisVerificationTest {
  @Mock private AnalysisRepository repository;
  @Mock private FileInfoService fileInfoService;
  @Mock private IdService idService;
  @Mock private CompositeEntityService compositeEntityService;
  @Mock private FileService fileService;
  @Mock private StorageService storageService;
  @Mock private SearchRepository searchRepository;
  @Mock private Sender sender;
  @Mock private StudyService studyService;
  @Mock private SampleSetRepository sampleSetRepository;
  @Mock private FileRepository fileRepository;
  @Mock private AnalysisDataRepository analysisDataRepository;
  @Mock private AnalysisTypeService analysisTypeService;
  @Mock private ValidationService validationService;

  AnalysisService setupTest(List<VerificationService> verificationServices) {
    when(repository.countAllByStudyIdAndAnalysisId(any(), any())).thenReturn(1L);
    val ann = new Analysis();
    ann.setAnalysisId("12345");
    ann.setAnalysisSchema(new AnalysisSchema());
    ann.setAnalysisData(new AnalysisData());
    when(repository.findOne((Specification<Analysis>) any())).thenReturn(java.util.Optional.of(ann));

    when(validationService.validateAnalysisTypeVersion(any(), any())).thenReturn(null);

    val schema = new AnalysisSchema();
    schema.setVersion(1);
    schema.setSchema(fromJson("{\"required\": [], \"properties\": {}}", JsonNode.class));
    when(analysisTypeService.getAnalysisSchema(any())).thenReturn(schema);

    return new AnalysisService("{\"required\": [], \"properties\":{}}", repository,
      fileInfoService, idService,
      compositeEntityService, fileService, storageService, searchRepository, sender, studyService, sampleSetRepository,
      fileRepository, analysisDataRepository, analysisTypeService, validationService, verificationServices);
  }

  @Test
  public void test_update_analysis_with_issues() {
    val issues = List.of("Analysis has no files section!");
    val verificationService = mock(VerificationService.class);
    when(verificationService.verify(any())).thenReturn(
      new VerifierReply(VerifierStatus.ISSUES, issues));

    val verifiers = List.of(verificationService);
    val analysisService = setupTest(verifiers);

    val RESOURCE_FETCHER =
      ResourceFetcher.builder()
        .resourceType(ResourceFetcher.ResourceType.TEST)
        .dataDir(Paths.get("documents/updateAnalysis"))
        .build();
    val update_request = RESOURCE_FETCHER.readJsonNode("variantcall1-valid-update-request.json");
    ServerException ex = null;
    try {
      analysisService.updateAnalysis("ABC123", "12345", update_request);
    } catch (ServerException e) {
      ex = e;
    }
    val prefix="bio.overture.song.core.exceptions.ServerException: "
      + "[VerificationService::payload.verification.failed] - Payload verification issues: ";
    assertEquals(prefix + issues.toString(), ex.toString());
  }

  @Test
  public void test_update_analysis_with_multiple_issues() {
    val issue1 ="Analysis has no files section!";
    val issue2 ="Sample was taken more than one year before donor's date of birth";
    val issue3 = "File abcd.bam.gz is missing corresponding BAI file";

    val issues = List.of(issue1, issue2, issue3);
    
    val verificationService1 = mock(VerificationService.class);
    when(verificationService1.verify(any())).thenReturn(
      new VerifierReply(VerifierStatus.ISSUES, List.of(issue1)));

    val verificationService2 = mock(VerificationService.class);
    when(verificationService2.verify(any())).thenReturn(
      new VerifierReply(VerifierStatus.ISSUES, List.of(issue2, issue3)));

    val verifiers = List.of(verificationService1, verificationService2);
    val analysisService = setupTest(verifiers);

    val RESOURCE_FETCHER =
      ResourceFetcher.builder()
        .resourceType(ResourceFetcher.ResourceType.TEST)
        .dataDir(Paths.get("documents/updateAnalysis"))
        .build();
    val update_request = RESOURCE_FETCHER.readJsonNode("variantcall1-valid-update-request.json");
    ServerException ex = null;
    try {
      analysisService.updateAnalysis("ABC123", "12345", update_request);
    } catch (ServerException e) {
      ex = e;
    }
    val prefix="bio.overture.song.core.exceptions.ServerException: "
      + "[VerificationService::payload.verification.failed] - Payload verification issues: ";
    assertEquals(prefix + issues.toString(), ex.toString());
  }


  @Test public void test_update_analysis_without_issues() {
    val issues = new ArrayList<String>();
    val verificationService = mock(VerificationService.class);
    when(verificationService.verify(any())).thenReturn(
      new VerifierReply(VerifierStatus.OK, issues));

    val verifiers = List.of(verificationService);
    val analysisService = setupTest(verifiers);

    val RESOURCE_FETCHER =
      ResourceFetcher.builder()
        .resourceType(ResourceFetcher.ResourceType.TEST)
        .dataDir(Paths.get("documents/updateAnalysis"))
        .build();
    val update_request = RESOURCE_FETCHER.readJsonNode("variantcall1-valid-update-request.json");
    ServerException ex = null;
    try {
      analysisService.updateAnalysis("ABC123", "12345", update_request);
    } catch (ServerException e) {
      ex = e;
    }

    assertNull(ex);
  }

  @Test public void test_update_analysis_with_verifier_error() {
    val issues = List.of("SyntaxError: invalid syntax");
    val verificationService = mock(VerificationService.class);
    when(verificationService.verify(any())).thenReturn(
      new VerifierReply(VerifierStatus.VERIFIER_ERROR, issues));

    val verifiers = List.of(verificationService);
    val analysisService = setupTest(verifiers);

    val RESOURCE_FETCHER =
      ResourceFetcher.builder()
        .resourceType(ResourceFetcher.ResourceType.TEST)
        .dataDir(Paths.get("documents/updateAnalysis"))
        .build();
    val update_request = RESOURCE_FETCHER.readJsonNode("variantcall1-valid-update-request.json");
    ServerException ex = null;
    try {
      analysisService.updateAnalysis("ABC123", "12345", update_request);
    } catch (ServerException e) {
      ex = e;
    }

    val prefix="bio.overture.song.core.exceptions.ServerException: "
      + "[VerificationService::bad.reply.from.gateway] - Verifier threw exception: ";
    assertEquals(prefix + issues.toString(), ex.toString());
  }

}
