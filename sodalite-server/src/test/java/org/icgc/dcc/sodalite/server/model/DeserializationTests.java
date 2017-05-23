package org.icgc.dcc.sodalite.server.model;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import lombok.val;

/**
 * Use this to escape/unescape the sample JSON documents
 * 
 * http://www.freeformatter.com/json-escape.html
 * 
 * @author ayang
 *
 */
public class DeserializationTests {

  private static ObjectMapper mapper = new ObjectMapper();

  @Test
  public void test() throws JsonParseException, JsonMappingException, IOException {
    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("documents/register-sequencingread-valid.json");
    // RegisterSequencingReadMessage obj = mapper.readValue(is,
    // RegisterSequencingReadMessage.class);
    System.out.println("");
  }

  @Test
  public void test_read_donor_json() throws JsonParseException, JsonMappingException, IOException {
    /*
     * {
  "donorSubmitterId": "Patient A",
  "donorGender": "male",
  "specimens": [
    {
      "specimenSubmitterId": "Specimen A-1",
      "specimenClass": "Normal",
      "specimenType": "Normal - solid tissue",
      "samples": [
        {
          "sampleSubmitterId": "Sample A1-a",
          "sampleType": "DNA",
          "files": [
            {
              "fileName": "A1a.bam",
              "fileSize": 100200300,
              "fileType": "BAM",
              "fileMd5": "ed5de7a6b9d86e0e5466e62b846b1b09"
            }
          ]
        }
      ]
    }
  ]
}
     */
    String json = "{\"donorSubmitterId\": \"Patient A\",\r\n \"donorGender\": \"male\",\r\n \"specimens\":[\r\n             {\"specimenSubmitterId\": \"Specimen A-1\",\r\n              \"specimenClass\": \"Normal\",\r\n              \"specimenType\": \"Normal - solid tissue\",\r\n              \"samples\": [\r\n                  {\r\n                      \"sampleSubmitterId\": \"Sample A1-a\",\r\n                      \"sampleType\": \"DNA\",\r\n                      \"files\": [{\r\n                          \"fileName\": \"A1a.bam\",\r\n                          \"fileSize\": 100200300,\r\n                          \"fileType\": \"BAM\", \r\n                          \"fileMd5\": \"ed5de7a6b9d86e0e5466e62b846b1b09\"\r\n                         \r\n                      }]\r\n                  }\r\n              ]\r\n             }\r\n    ]\r\n}";
    
    ObjectReader reader = mapper.readerFor(Donor.class);
    val obj = reader.readValue(json);
    
    ObjectWriter writer = mapper.writerFor(Donor.class);
    val str = writer.writeValueAsString(obj);
    
    //RegisterSequencingReadMessage obj = mapper.readValue(is, RegisterSequencingReadMessage.class);
    System.out.println("");
  }
  
  @Test
  public void test_read_study() throws JsonParseException, JsonMappingException, IOException {
    String json = "{\r\n  \"sequencingRead\": {\r\n    \"aligned\": true,\r\n    \"alignmentTool\": \"deserunt velit mollit\",\r\n    \"insertSize\": 28042512,\r\n    \"libraryStrategy\": \"RNA-Seq\",\r\n    \"pairedEnd\": false,\r\n    \"referenceGenome\": \"qui amet\"\r\n  },\r\n  \"study\": {\r\n    \"name\": \"Duis sint\",\r\n    \"organization\": \"enim\",\r\n    \"description\": \"velit anim est\",\r\n    \"donor\": {\r\n      \"donorSubmitterId\": \"incididunt fugiat\",\r\n      \"donorGender\": \"male\",\r\n      \"specimen\": {\r\n        \"specimenSubmitterId\": \"submissius plodicus\",\r\n        \"specimenClass\": \"Normal\",\r\n        \"specimenType\": \"Normal - blood derived\",\r\n        \"sample\": {\r\n          \"sampleSubmitterId\": \"est sed id\",\r\n          \"sampleType\": \"RNA\",\r\n          \"files\": [\r\n            {\r\n              \"fileName\": \"magna\",\r\n              \"fileSize\": 59857573,\r\n              \"fileType\": \"IDX\",\r\n              \"fileMd5\": \"occaecat\"\r\n            },\r\n            {\r\n              \"fileName\": \"deserunt fugiat enim ci\",\r\n              \"fileSize\": 8897683,\r\n              \"fileType\": \"FASTA\",\r\n              \"fileMd5\": \"minim reprehenderit\"\r\n            },\r\n            {\r\n              \"fileName\": \"incidid\",\r\n              \"fileSize\": 86761872,\r\n              \"fileType\": \"XML\",\r\n              \"fileMd5\": \"ad exercitation\"\r\n            },\r\n            {\r\n              \"fileName\": \"id\",\r\n              \"fileSize\": 21090,\r\n              \"fileType\": \"XML\",\r\n              \"fileMd5\": \"in aliqua ad id\"\r\n            }\r\n          ]\r\n        }\r\n      }\r\n    }\r\n  }\r\n}";
    String studyJson = "{\r\n    \"name\": \"Duis sint\",\r\n    \"organization\": \"enim\",\r\n    \"description\": \"velit anim est\",\r\n    \"donor\": {\r\n      \"donorSubmitterId\": \"incididunt fugiat\",\r\n      \"donorGender\": \"male\",\r\n      \"specimen\": {\r\n        \"specimenSubmitterId\": \"submissius plodicus\",\r\n        \"specimenClass\": \"Normal\",\r\n        \"specimenType\": \"Normal - blood derived\",\r\n        \"sample\": {\r\n          \"sampleSubmitterId\": \"est sed id\",\r\n          \"sampleType\": \"RNA\",\r\n          \"files\": [\r\n            {\r\n              \"fileName\": \"magna\",\r\n              \"fileSize\": 59857573,\r\n              \"fileType\": \"IDX\",\r\n              \"fileMd5\": \"occaecat\"\r\n            },\r\n            {\r\n              \"fileName\": \"deserunt fugiat enim ci\",\r\n              \"fileSize\": 8897683,\r\n              \"fileType\": \"FASTA\",\r\n              \"fileMd5\": \"minim reprehenderit\"\r\n            },\r\n            {\r\n              \"fileName\": \"incidid\",\r\n              \"fileSize\": 86761872,\r\n              \"fileType\": \"XML\",\r\n              \"fileMd5\": \"ad exercitation\"\r\n            },\r\n            {\r\n              \"fileName\": \"id\",\r\n              \"fileSize\": 21090,\r\n              \"fileType\": \"XML\",\r\n              \"fileMd5\": \"in aliqua ad id\"\r\n            }\r\n          ]\r\n        }\r\n      }\r\n    }\r\n}";
    
    ObjectReader reader = mapper.readerFor(Study.class);
    JsonNode node = mapper.reader().readTree(studyJson);
    
    val obj1 = reader.readValue(node);
    val obj = reader.readValue(studyJson);
    System.out.println();
  }
  
  @Test
  public void test_read_doc() throws JsonParseException, JsonMappingException, IOException {
    String json = "{\r\n  \"sequencingRead\": {\r\n    \"aligned\": true,\r\n    \"alignmentTool\": \"deserunt velit mollit\",\r\n    \"insertSize\": 28042512,\r\n    \"libraryStrategy\": \"RNA-Seq\",\r\n    \"pairedEnd\": false,\r\n    \"referenceGenome\": \"qui amet\"\r\n  },\r\n  \"study\": {\r\n    \"name\": \"Duis sint\",\r\n    \"organization\": \"enim\",\r\n    \"description\": \"velit anim est\",\r\n    \"donor\": {\r\n      \"donorSubmitterId\": \"incididunt fugiat\",\r\n      \"donorGender\": \"male\",\r\n      \"specimen\": {\r\n        \"specimenSubmitterId\": \"submissius plodicus\",\r\n        \"specimenClass\": \"Normal\",\r\n        \"specimenType\": \"Normal - blood derived\",\r\n        \"sample\": {\r\n          \"sampleSubmitterId\": \"est sed id\",\r\n          \"sampleType\": \"RNA\",\r\n          \"files\": [\r\n            {\r\n              \"fileName\": \"magna\",\r\n              \"fileSize\": 59857573,\r\n              \"fileType\": \"IDX\",\r\n              \"fileMd5\": \"occaecat\"\r\n            },\r\n            {\r\n              \"fileName\": \"deserunt fugiat enim ci\",\r\n              \"fileSize\": 8897683,\r\n              \"fileType\": \"FASTA\",\r\n              \"fileMd5\": \"minim reprehenderit\"\r\n            },\r\n            {\r\n              \"fileName\": \"incidid\",\r\n              \"fileSize\": 86761872,\r\n              \"fileType\": \"XML\",\r\n              \"fileMd5\": \"ad exercitation\"\r\n            },\r\n            {\r\n              \"fileName\": \"id\",\r\n              \"fileSize\": 21090,\r\n              \"fileType\": \"XML\",\r\n              \"fileMd5\": \"in aliqua ad id\"\r\n            }\r\n          ]\r\n        }\r\n      }\r\n    }\r\n  }\r\n}";

    ObjectReader reader = mapper.reader();
    JsonNode root = reader.readTree(json);

    JsonNode analysisObject = root.path("sequencingRead");
    if (analysisObject.isMissingNode()) {
      analysisObject = root.path("variantCall");
    } 
      
    if (analysisObject.isMissingNode()) {
      System.out.println(String.format("Unrecognized Analysis Object %s", root.textValue()));
    } 

    JsonNode metadata = root.path("study");
    if (metadata.isMissingNode()) {
      System.out.println((String.format("Could not find metadata", root.textValue()))); 
    }

    ObjectReader studyReader = mapper.readerFor(Study.class);
    val study = studyReader.readValue(metadata);

    System.out.println();
  }
}
