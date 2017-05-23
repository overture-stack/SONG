package org.icgc.dcc.sodalite.server.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.flywaydb.test.annotation.FlywayTest;
import org.flywaydb.test.junit.FlywayTestExecutionListener;
import org.h2.tools.Server;
import org.icgc.dcc.sodalite.server.model.AnalysisState;
import org.icgc.dcc.sodalite.server.model.File;
import org.icgc.dcc.sodalite.server.model.FileType;
import org.icgc.dcc.sodalite.server.model.LibraryStrategy;
import org.icgc.dcc.sodalite.server.model.SequencingRead;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import lombok.val;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, FlywayTestExecutionListener.class })
@FlywayTest
@ActiveProfiles("dev")
public class SequencingReadServiceTest {

  @Autowired
  SequencingReadService readService;
  @Autowired
  FileService fileService;
  @Autowired
  SequencingReadFileAssociationService associationService;

  // allows connection to H2 console from browser
  Server webServer;

  // "studyId", "analysisId", "state", "aligned", "alignmentTool", "insertSize", "libraryStrategy", "pairedEnd",
  // "referenceGenome"

  @Before
  public void initTest() throws SQLException {
    webServer = Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082");
    webServer.start();
  }

  @After
  public void stopServer() {
    webServer.stop();
  }

  public SequencingRead createRead() {
    val result = new SequencingRead();
    result.withStudyId("ABC123")
        .withAnalysisSubmitterId("101-IP-A")
        .withState(AnalysisState.RECEIVED)
        .withAligned(true)
        .withAlignmentTool("Scotch Tape")
        .withInsertSize(2000)
        .withLibraryStrategy(LibraryStrategy.OTHER)
        .withPairedEnd(true)
        .withReferenceGenome("something something");
    return result;
  }

  public List<File> createFileSet() {
    val bamf = new File()
        .withStudyId("ABC123")
        .withSampleId("SA1")
        .withFileName("ABC-TC285G87-A5-sqrl.bam")
        .withFileSize(50000000000L)
        .withFileType(FileType.BAM);
    fileService.create(bamf);

    val baif = new File()
        .withStudyId("ABC123")
        .withSampleId("SA1")
        .withFileName("ABC-TC285G87-A5-sqrl.bai")
        .withFileSize(50000)
        .withFileType(FileType.BAI);
    fileService.create(baif);

    val xmlf = new File()
        .withStudyId("ABC123")
        .withSampleId("SA1")
        .withFileName("hamsters.xml")
        .withFileSize(5000)
        .withFileType(FileType.XML);
    fileService.create(xmlf);

    return Arrays.asList(bamf, baif, xmlf);
  }

  @Test
  public void test_create_sequencing_read_record() {
    SequencingRead read = createRead();
    val newId = readService.create(read);
    val reread = readService.getById(newId);
    assertThat(read).isEqualTo(reread);
  }

  @Test
  public void test_associations() {
    SequencingRead read = createRead();
    val newId = readService.create(read);

    List<File> files = createFileSet();

    for (File f : files) {
      associationService.associate(read, f);
    }

    val associations = associationService.getFileIds(read.getAnalysisId());
    assertThat(associations.size()).isEqualTo(3);
  }

}
