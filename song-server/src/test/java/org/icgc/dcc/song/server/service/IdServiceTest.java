package org.icgc.dcc.song.server.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.server.model.enums.IdPrefix;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
@ActiveProfiles({"dev", "secure", "test"})
public class IdServiceTest {

  @Autowired
  IdService idService;

  @Test
  public void testIdempotentDonorId() {
    runIdempotentIdTest(() -> idService.generateDonorId("t1", "study1"), true);
  }
  @Test
  public void testIdempotentFileId() {
    runIdempotentIdTest(() -> idService.generateFileId("an1", "f1"), true);
  }
  @Test
  public void testIdempotentSampleId() {
    runIdempotentIdTest(() -> idService.generateSampleId("sub_sa_1", "study1"), true);
  }

  @Test
  public void testIdempotentSpecimenId() {
    runIdempotentIdTest(() -> idService.generateSpecimenId("sub_sp_1", "study1"), true);
  }
  @Test
  public void testNonIdempotentRandomId() {
    runIdempotentIdTest(() -> idService.generate(IdPrefix.ANALYSIS_PREFIX), false);
  }

  @Test
  public void testNonIdempotentAnalysisId() {
    runIdempotentIdTest(() -> idService.generateAnalysisId(), false);
  }

  private void runIdempotentIdTest(Supplier<String> idSupplier, boolean isIdempotent){
    val id1 = idSupplier.get();
    val id2 = idSupplier.get();
    if (isIdempotent){
      assertThat(id1).isEqualTo(id2);
    } else {
      assertThat(id1).isNotEqualTo(id2);
    }
  }

}
