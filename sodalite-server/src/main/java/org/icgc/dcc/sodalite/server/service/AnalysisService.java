package org.icgc.dcc.sodalite.server.service;

import static java.lang.String.format;

import java.util.List;
import java.util.Map;

import org.icgc.dcc.sodalite.server.model.Analysis;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@RequiredArgsConstructor

public class AnalysisService {
	private void info(String fmt, Object... args) {
		log.info(format(fmt, args));
	}
	public int CreateOrUpdateAnalysis(String json) {
		// TODO Auto-generated method stub
		info("Called CreateOrUpdateAnalysis with '%s'", json);
		return 0;
	}

	public List<Analysis> getAnalysisById(String id) {
		// TODO Auto-generated method stub
		info("Called GetAnalysisById with %s",id);
		return null;
	}

	public List<Analysis> getAnalyses(Map<String, String> params) {
		info("Called getAnalyses with %s",params);
		// TODO Auto-generated method stub
		return null;
	}

}
