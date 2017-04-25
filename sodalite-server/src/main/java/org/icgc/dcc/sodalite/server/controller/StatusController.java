package org.icgc.dcc.sodalite.server.controller;

import java.util.Map;

import org.icgc.dcc.sodalite.server.service.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path="/studies/{study_id}/statuses")
@RequiredArgsConstructor
public class StatusController {
	@Autowired
	private final StatusService statusService;

	  @GetMapping(value="/{id}")
	  @ResponseBody
	  public int notifyUpload(@PathVariable("id") String id) {
		  return statusService.getRegistrationState(id);
	  }
	  
	  @GetMapping()
	  @ResponseBody
	  public int publish(@RequestParam Map<String, String> params) {
		  return statusService.getRegistrationStates(params);
	  }

}
