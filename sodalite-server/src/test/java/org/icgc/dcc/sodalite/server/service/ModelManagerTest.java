package org.icgc.dcc.sodalite.server.service;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.flywaydb.test.annotation.FlywayTest;
import org.flywaydb.test.junit.FlywayTestExecutionListener;
import org.h2.tools.Server;
import org.icgc.dcc.sodalite.server.model.File;
import org.icgc.dcc.sodalite.server.model.SequencingRead;
import org.icgc.dcc.sodalite.server.model.TestDataFactory;
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
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.val;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, FlywayTestExecutionListener.class })
@FlywayTest
@ActiveProfiles("dev")
public class ModelManagerTest {

  @Autowired
  ModelManager sut;
  
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
  public void test() throws JsonProcessingException, IOException {
    String analysisSubmitterId = "1fbb6bb0-caca-4c4f-8f11-72bfa9a5faef";
    ObjectMapper mapper = new ObjectMapper();
    String json = "{\r\n  \"sequencingRead\": {\r\n    \"aligned\": true,\r\n    \"alignmentTool\": \"deserunt velit mollit\",\r\n    \"insertSize\": 28042512,\r\n    \"libraryStrategy\": \"RNA-Seq\",\r\n    \"pairedEnd\": false,\r\n    \"referenceGenome\": \"qui amet\"\r\n  },\r\n  \"study\": {\r\n    \"name\": \"Duis sint\",\r\n    \"organization\": \"enim\",\r\n    \"description\": \"velit anim est\",\r\n    \"donor\": {\r\n      \"donorSubmitterId\": \"incididunt fugiat\",\r\n      \"donorGender\": \"male\",\r\n      \"specimen\": {\r\n        \"specimenSubmitterId\": \"submissius plodicus\",\r\n        \"specimenClass\": \"Normal\",\r\n        \"specimenType\": \"Normal - blood derived\",\r\n        \"sample\": {\r\n          \"sampleSubmitterId\": \"est sed id\",\r\n          \"sampleType\": \"RNA\",\r\n          \"files\": [\r\n            {\r\n              \"fileName\": \"magna\",\r\n              \"fileSize\": 59857573,\r\n              \"fileType\": \"IDX\",\r\n              \"fileMd5\": \"occaecat\"\r\n            },\r\n            {\r\n              \"fileName\": \"deserunt fugiat enim ci\",\r\n              \"fileSize\": 8897683,\r\n              \"fileType\": \"FASTA\",\r\n              \"fileMd5\": \"minim reprehenderit\"\r\n            },\r\n            {\r\n              \"fileName\": \"incidid\",\r\n              \"fileSize\": 86761872,\r\n              \"fileType\": \"XML\",\r\n              \"fileMd5\": \"ad exercitation\"\r\n            },\r\n            {\r\n              \"fileName\": \"id\",\r\n              \"fileSize\": 21090,\r\n              \"fileType\": \"XML\",\r\n              \"fileMd5\": \"in aliqua ad id\"\r\n            }\r\n          ]\r\n        }\r\n      }\r\n    }\r\n  }\r\n}";
    val jsonNode = mapper.reader().readTree(json);
    sut.persist("ABC123", analysisSubmitterId, jsonNode);
    
    val doc = sut.loadSequencingRead("ABC123", analysisSubmitterId);
    System.out.println();
  }
  
  @Test
  public void wtf() {
    
  }

}
