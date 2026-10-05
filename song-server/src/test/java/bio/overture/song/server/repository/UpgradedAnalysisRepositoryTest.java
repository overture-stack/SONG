package bio.overture.song.server.repository;

import static bio.overture.song.core.model.enums.AnalysisStates.PUBLISHED;
import static bio.overture.song.core.model.enums.AnalysisStates.SUPPRESSED;
import static bio.overture.song.core.model.enums.AnalysisStates.UNPUBLISHED;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.utils.generator.AnalysisGenerator.createAnalysisGenerator;
import static bio.overture.song.server.utils.generator.StudyGenerator.createStudyGenerator;
import static org.junit.jupiter.api.Assertions.assertEquals;

import bio.overture.song.core.model.enums.AnalysisStates;
import bio.overture.song.server.service.StudyService;
import bio.overture.song.server.service.analysis.AnalysisService;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Tests the native count query behind the paginated analysis endpoint's {@code totalAnalyses}.
 *
 * <p>The count is read as a {@link Number} so the test is independent of the Java type the query
 * result is mapped to, which differs between Hibernate versions.
 */
@Transactional
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class UpgradedAnalysisRepositoryTest {

  @Autowired private UpgradedAnalysisRepository upgradedAnalysisRepository;
  @Autowired private AnalysisRepository analysisRepository;
  @Autowired private StudyService studyService;
  @Autowired private AnalysisService analysisService;
  @Autowired private EntityManager entityManager;

  private String studyId;
  private String otherStudyId;

  @BeforeEach
  public void beforeEachTest() {
    val randomGenerator = createRandomGenerator(getClass().getSimpleName());
    val studyGenerator = createStudyGenerator(studyService, randomGenerator);
    this.studyId = studyGenerator.createRandomStudy();
    this.otherStudyId = studyGenerator.createRandomStudy();
    val analysisGenerator = createAnalysisGenerator(studyId, analysisService, randomGenerator);
    for (val state : new AnalysisStates[] {UNPUBLISHED, UNPUBLISHED, PUBLISHED, SUPPRESSED}) {
      val analysisId = analysisGenerator.createDefaultRandomVariantCallAnalysis().getAnalysisId();
      val analysis = analysisRepository.findById(analysisId).get();
      analysis.setAnalysisState(state.name());
    }
    entityManager.flush();
  }

  private long count(String countedStudyId, Set<String> states) {
    final Number total = upgradedAnalysisRepository.getTotalAnalysisCount(countedStudyId, states);
    return total.longValue();
  }

  @Test
  public void getTotalAnalysisCount_singleState() {
    assertEquals(2, count(studyId, Set.of(UNPUBLISHED.name())));
    assertEquals(1, count(studyId, Set.of(PUBLISHED.name())));
    assertEquals(1, count(studyId, Set.of(SUPPRESSED.name())));
  }

  @Test
  public void getTotalAnalysisCount_multipleStates() {
    assertEquals(2, count(studyId, Set.of(PUBLISHED.name(), SUPPRESSED.name())));
    assertEquals(
        4, count(studyId, Set.of(UNPUBLISHED.name(), PUBLISHED.name(), SUPPRESSED.name())));
  }

  @Test
  public void getTotalAnalysisCount_otherStudy_zero() {
    assertEquals(0, count(otherStudyId, Set.of(UNPUBLISHED.name(), PUBLISHED.name())));
  }
}
