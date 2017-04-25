package org.icgc.dcc.sodalite.server.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.icgc.dcc.sodalite.server.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path="/studies/{study_id}/func/")
@RequiredArgsConstructor
public class FunctionController {
	@Autowired
	private final UploadService uploadService;

	  @PostMapping(value="notify-upload", consumes = {APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE})
	  @ResponseBody
	  public int notifyUpload(@PathVariable("study_id") String id) {
		  return uploadService.notifyUpload(id);
	  }
	  
	  @PostMapping(value="publish", consumes = {APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE})
	  @ResponseBody
	  public int publish(@PathVariable("study_id") String id) {
		  return uploadService.publish(id);
	  }

}
