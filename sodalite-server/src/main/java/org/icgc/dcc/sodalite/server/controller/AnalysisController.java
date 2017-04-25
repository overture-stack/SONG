package org.icgc.dcc.sodalite.server.controller;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import org.icgc.dcc.sodalite.server.model.Analysis;
import org.icgc.dcc.sodalite.server.model.json.register.RegisterSequencingReadMessage;
import org.icgc.dcc.sodalite.server.model.json.register.RegisterVariantCallMessage;
import org.icgc.dcc.sodalite.server.model.json.update.analysis.SequencingReadUpdateMessage;
import org.icgc.dcc.sodalite.server.model.json.update.analysis.VariantCallUpdateMessage;
import org.icgc.dcc.sodalite.server.service.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/studies/{study_id}/analyses")
public class AnalysisController {
	
  /**
   * Dependencies
   */
  @Autowired
  private final AnalysisService analysisService;

  @PostMapping(consumes = {APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE})
  @ResponseBody
  @SneakyThrows
  public int createAnalysis(@RequestBody String json) {
	ObjectMapper mapper=new ObjectMapper();
	JsonNode node = mapper.readTree(json);
	
	if (node.has("sequencingReadSubmission")) {
		RegisterSequencingReadMessage m=mapper.readValue(json, RegisterSequencingReadMessage.class);
		return analysisService.registerSequencingRead(m.getSequencingReadSubmission());
	} else if (node.has("variantCallSubmission")) {
		RegisterVariantCallMessage m=mapper.readValue(json, RegisterVariantCallMessage.class);
		return analysisService.registerVariantCall(m.getVariantCallSubmission());
	}
	return -1;
    
  }
  
  @PutMapping(consumes = {APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE})
  @ResponseBody
  @SneakyThrows
  public int modifyAnalysis(@RequestBody String json) {
	ObjectMapper mapper=new ObjectMapper(); 
	JsonNode node = mapper.readTree(json);
	
	if (node.has("sequencingReadUpdate")) {
		// create this from the JSON string somehow...
		SequencingReadUpdateMessage m = mapper.readValue(json, SequencingReadUpdateMessage.class);
		return analysisService.updateSequencingRead(m.getSequencingReadUpdate());
	} else if (node.has("variantUpdateCall")) {
		VariantCallUpdateMessage m = mapper.readValue(json, VariantCallUpdateMessage.class);
		return analysisService.updateVariantCall(m.getVariantCallUpdate());
	}
	return -1;
  }
  
  @GetMapping(value="/{id}")
  public List<Analysis> GetAnalysisyById(@PathVariable("id") String id) {
    return analysisService.getAnalysisById(id);
  }

  @GetMapping(value="")
  public List<Analysis> getAnalyses(@RequestParam Map<String, String> params) {
	  return analysisService.getAnalyses(params);
  }
  
   
}

