package org.icgc.dcc.sodalite.server.model;

import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.List;

import org.flywaydb.test.annotation.FlywayTest;
import org.flywaydb.test.junit.FlywayTestExecutionListener;
import org.h2.tools.Server;
import org.icgc.dcc.sodalite.server.model.utils.Views;
import org.icgc.dcc.sodalite.server.service.DonorService;
import org.icgc.dcc.sodalite.server.service.FileService;
import org.icgc.dcc.sodalite.server.service.ModelManager;
import org.icgc.dcc.sodalite.server.service.SampleService;
import org.icgc.dcc.sodalite.server.service.SequencingReadFileAssociationService;
import org.icgc.dcc.sodalite.server.service.SequencingReadService;
import org.icgc.dcc.sodalite.server.service.SpecimenService;
import org.icgc.dcc.sodalite.server.service.StudyService;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import lombok.val;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, FlywayTestExecutionListener.class })
@FlywayTest
@ActiveProfiles("dev")
public class SerializationTests {

  @Autowired
  StudyService studyService;
  @Autowired
  DonorService donorService;
  @Autowired
  SpecimenService specimenService;
  @Autowired
  SampleService sampleService;
  @Autowired
  FileService fileService;
  
  @Autowired
  SequencingReadService readService;
  @Autowired
  SequencingReadFileAssociationService associationService;

  @Autowired
  ModelManager manager;
  
  Server webServer;  // allows connection to H2 console from browser

  @Before
  public void initTest() throws SQLException{
    webServer = Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082");
    webServer.start();
  } 
  
  @After
  public void stopServer() {
    webServer.stop();
  }

  @Test
  public void test() throws JsonProcessingException {
    SequencingRead read = TestDataFactory.createSequencingRead();
    readService.create(read);
    
    val donorId = donorService.create(TestDataFactory.createDonor());
    val specimenId = specimenService.create(TestDataFactory.createSpecimen(donorId));
    val sampleId = sampleService.create(TestDataFactory.createSample(specimenId));
    
    List<File> fileset = TestDataFactory.createFileSet(sampleId);
    for (File f : fileset) {
      val oid = fileService.create(f);
      f.setObjectId(oid);
      associationService.associate(read, f);
    }
    
    val reread = manager.loadSequencingRead(read.getAnalysisId());
    
    ObjectMapper mapper = new ObjectMapper();
    ObjectWriter writer = mapper.writerWithView(Views.Document.class);
    String json = writer.writeValueAsString(reread);
    
    //ObjectWriter writer = new ObjectMapper().writerFor(SequencingReadDocument.class);
    // val json = writer.writeValueAsString(reread);

    System.out.println(json);
  }

}
